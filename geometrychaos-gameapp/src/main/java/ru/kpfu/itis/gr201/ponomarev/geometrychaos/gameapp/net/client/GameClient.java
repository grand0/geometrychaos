package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.net.client;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.GameApplication;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapData;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.DisconnectionReason;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.ShallowPlayer;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message.*;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.server.ServerConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class GameClient {

    private static final int CONNECTION_TIMEOUT = 5000;
    private static final long PLAYER_UPDATE_INTERVAL_NS = 1_000_000_000 / 10;

    private static GameClient instance;
    private GameApplication app;
    private Socket socket;
    private ServerCommunicator serverCommunicator;

    private byte[] mapBytes;

    private long lastThisPlayerUpdateTime;

    private GameClient() {}

    public static GameClient getInstance() {
        if (instance == null) {
            instance = new GameClient();
        }
        return instance;
    }

    public void initApplication(GameApplication app) {
        this.app = app;
    }

    public void connect(String username, String host) throws IOException {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }

        int port = ServerConfig.PORT;
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        serverCommunicator = new ServerCommunicator(in, out, this);
        new Thread(serverCommunicator).start();

        PlayerConnectionRequestMessage message = new PlayerConnectionRequestMessage(username);
        serverCommunicator.writeMessage(message);
    }

    public void disconnect() {
        app.clearPlayers();
        if (serverCommunicator != null) {
            if (serverCommunicator.getThisPlayerId() != null) {
                PlayerDisconnectedMessage disconnectedMessage = new PlayerDisconnectedMessage(serverCommunicator.getThisPlayerId(), DisconnectionReason.LEFT);
                try {
                    serverCommunicator.writeMessage(disconnectedMessage);
                } catch (IOException ignored) {}
            }
            serverCommunicator.stop();
            serverCommunicator = null;
        }
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {}
            socket = null;
        }
    }

    public void uploadSelectedMap() throws IOException {
        if (!isConnected()) return;

        GameMapData data = app.getSelectedGameMap();
        MapChangeMessage mapChangeMessage = new MapChangeMessage(data.data().length, data.name());
        serverCommunicator.writeMessage(mapChangeMessage);

        try (ByteArrayInputStream in = new ByteArrayInputStream(data.data())) {
            byte[] buf = new byte[16384];
            int totalUploaded = 0;
            while (in.available() != 0) {
                int len = in.read(buf);
                MapChunkMessage mapChunkMessage = new MapChunkMessage(totalUploaded, Arrays.copyOf(buf, len));
                serverCommunicator.writeMessage(mapChunkMessage);
                totalUploaded += len;
            }
        }
    }

    public void startMapDownloading(int size) throws IOException {
        if (!isConnected()) return;

        mapBytes = new byte[size];

        MapChunkRequestMessage mapChunkRequestMessage = new MapChunkRequestMessage(0);
        serverCommunicator.writeMessage(mapChunkRequestMessage);
    }

    public void thisPlayerReady() {
        if (!isConnected()) return;

        PlayerReadyMessage readyMessage = new PlayerReadyMessage(serverCommunicator.getThisPlayerId());
        try {
            serverCommunicator.writeMessage(readyMessage);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            disconnect();
            app.clientDisconnectedWithError(e);
        }
    }

    public void saveChunkAndRequestNext(int offset, byte[] chunk) {
        if (!isConnected()) return;

        System.arraycopy(chunk, 0, mapBytes, offset, chunk.length);
        try {
            if (offset + chunk.length == mapBytes.length) {
                app.mapDownloaded(mapBytes);
                MapDownloadedMessage mapDownloadedMessage = new MapDownloadedMessage(serverCommunicator.getThisPlayerId());
                serverCommunicator.writeMessage(mapDownloadedMessage);
            } else {
                MapChunkRequestMessage mapChunkRequestMessage = new MapChunkRequestMessage(offset + chunk.length);
                serverCommunicator.writeMessage(mapChunkRequestMessage);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            disconnect();
            app.clientDisconnectedWithError(e);
        }
    }

    public void thisPlayerHit(int healthPoints) {
        if (!isConnected()) return;

        PlayerHitMessage hitMessage = new PlayerHitMessage(serverCommunicator.getThisPlayerId(), healthPoints);
        try {
            serverCommunicator.writeMessage(hitMessage);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            disconnect();
            app.clientDisconnectedWithError(e);
        }
    }

    public void thisPlayerDash(double velocityX, double velocityY) {
        if (!isConnected()) return;

        PlayerDashMessage dashMessage = new PlayerDashMessage(serverCommunicator.getThisPlayerId(), velocityX, velocityY);
        try {
            serverCommunicator.writeMessage(dashMessage);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            disconnect();
            app.clientDisconnectedWithError(e);
        }
    }

    public void thisPlayerUpdate(double positionX, double positionY, double velocityX, double velocityY, int healthPoints) {
        if (!isConnected()) return;

        if (System.nanoTime() - lastThisPlayerUpdateTime >= PLAYER_UPDATE_INTERVAL_NS) {
            lastThisPlayerUpdateTime = System.nanoTime();
            PlayerUpdateMessage playerUpdateMessage = new PlayerUpdateMessage(serverCommunicator.getThisPlayerId(), positionX, positionY, velocityX, velocityY, healthPoints);
            try {
                serverCommunicator.writeMessage(playerUpdateMessage);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                disconnect();
                app.clientDisconnectedWithError(e);
            }
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case PLAYER_CONNECTION_APPROVED -> {
                PlayerConnectionApprovedMessage approveMessage = (PlayerConnectionApprovedMessage) message;
                serverCommunicator.setThisPlayerId(approveMessage.getThisPlayer().playerId());
                app.initThisPlayer(approveMessage.getThisPlayer().playerId(), approveMessage.getThisPlayer().username());
                for (ShallowPlayer player : approveMessage.getPlayers()) {
                    if (!player.equals(approveMessage.getThisPlayer())) {
                        app.addPlayer(player.playerId(), player.username());
                    }
                }
                app.connectionApproved();
            }
            case PLAYER_CONNECTION_DENIAL -> {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ignored) {}
                PlayerConnectionDenialMessage denialMessage = (PlayerConnectionDenialMessage) message;
                app.connectionDenied(denialMessage.getDenialReason());
            }
            case PLAYER_CONNECTED -> {
                PlayerConnectedMessage connectedMessage = (PlayerConnectedMessage) message;
                app.addPlayer(connectedMessage.getPlayerId(), connectedMessage.getUsername());
            }
            case PLAYER_DISCONNECTED -> {
                if (serverCommunicator != null) { // disconnect() method wasn't called
                    PlayerDisconnectedMessage disconnectedMessage = (PlayerDisconnectedMessage) message;
                    if (disconnectedMessage.getPlayerId() == serverCommunicator.getThisPlayerId()) {
                        disconnect();
                    } else {
                        // TODO: give disconnection reason to app
                        app.removePlayer(disconnectedMessage.getPlayerId());
                    }
                }
            }
            case MAP_DOWNLOADED -> {
                MapDownloadedMessage mapDownloadedMessage = (MapDownloadedMessage) message;
                if (mapDownloadedMessage.getPlayerId() == null) {
                    app.mapUploaded();
                } else {
                    app.playerDownloadedMap(mapDownloadedMessage.getPlayerId());
                }
            }
            case MAP_CHANGE -> {
                MapChangeMessage mapChangeMessage = (MapChangeMessage) message;
                app.mapChanged(mapChangeMessage.getName(), mapChangeMessage.getSize());
            }
            case MAP_CHUNK -> {
                MapChunkMessage mapChunkMessage = (MapChunkMessage) message;
                saveChunkAndRequestNext(mapChunkMessage.getOffset(), mapChunkMessage.getMapChunk());
            }
            case PLAYER_READY -> {
                PlayerReadyMessage readyMessage = (PlayerReadyMessage) message;
                app.playerReady(readyMessage.getPlayerId());
            }
            case GAME_STARTED -> {
                GameStartedMessage gameStartedMessage = (GameStartedMessage) message;
                app.startGame(gameStartedMessage.getSeed());
            }
            case PLAYER_UPDATE -> {
                PlayerUpdateMessage playerUpdateMessage = (PlayerUpdateMessage) message;
                for (Player player : app.getPlayers()) {
                    if (player.getPlayerId() == playerUpdateMessage.getPlayerId()) {
                        player.setPositionX(playerUpdateMessage.getPositionX());
                        player.setPositionY(playerUpdateMessage.getPositionY());
                        player.setVelocityX(playerUpdateMessage.getVelocityX());
                        player.setVelocityY(playerUpdateMessage.getVelocityY());
                        if (playerUpdateMessage.getHealthPoints() < player.getHealthPoints()) {
                            app.playerHit(playerUpdateMessage.getPlayerId(), playerUpdateMessage.getHealthPoints());
                        } else {
                            player.setHealthPoints(playerUpdateMessage.getHealthPoints());
                        }
                        break;
                    }
                }
            }
            case PLAYER_DASH -> {
                PlayerDashMessage dashMessage = (PlayerDashMessage) message;
                for (Player player : app.getPlayers()) {
                    if (player.getPlayerId() == dashMessage.getPlayerId()) {
                        player.setVelocityX(dashMessage.getVelocityX());
                        player.setVelocityY(dashMessage.getVelocityY());
                        player.dash();
                        break;
                    }
                }
            }
            case PLAYER_HIT -> {
                PlayerHitMessage hitMessage = (PlayerHitMessage) message;
                app.playerHit(hitMessage.getPlayerId(), hitMessage.getHealthPoints());
            }
        }
    }

    public boolean isConnected() {
        return serverCommunicator != null && serverCommunicator.isConnected;
    }

    private static class ServerCommunicator implements Runnable {

        private final InputStream input;
        private final OutputStream output;
        private final GameClient gameClient;
        private Integer thisPlayerId;

        private boolean isConnected = true;

        public ServerCommunicator(InputStream input, OutputStream output, GameClient gameClient) {
            this.input = input;
            this.output = output;
            this.gameClient = gameClient;
        }

        private void handleMessage(Message message) {
            switch (message.getType()) {
                case PLAYER_DISCONNECTED -> {
                    PlayerDisconnectedMessage disconnectedMessage = (PlayerDisconnectedMessage) message;
                    if (disconnectedMessage.getPlayerId() == thisPlayerId) {
                        isConnected = false;
                    }
                }
                case PLAYER_CONNECTION_DENIAL -> isConnected = false;
            }
            gameClient.handleMessage(message);
        }

        @Override
        public void run() {
            try {
                while (isConnected) {
                    Message message = Message.createMessage(input);
                    handleMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
                PlayerDisconnectedMessage disconnectedMessage = new PlayerDisconnectedMessage(thisPlayerId, DisconnectionReason.LEFT);
                gameClient.handleMessage(disconnectedMessage);
            }
        }

        public void writeMessage(Message message) throws IOException {
            output.write(message.getRaw());
        }

        public Integer getThisPlayerId() {
            return thisPlayerId;
        }

        public void setThisPlayerId(Integer thisPlayerId) {
            this.thisPlayerId = thisPlayerId;
        }

        public void stop() {
            isConnected = false;
        }
    }
}
