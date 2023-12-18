package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.server;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.model.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.ConnectionDenialReason;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.DisconnectionReason;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.ShallowPlayer;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message.*;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.IntStream;

public class GameServer {

    private static final int MAX_PLAYERS_COUNT = 4;
    private static final Logger LOGGER = System.getLogger(GameServer.class.getName());

    private final List<GameClient> clients = new LinkedList<>();

    private final NavigableSet<Integer> freePlayerIds = Collections.synchronizedNavigableSet(new TreeSet<>());

    private boolean gameStarted = false;

    private String currentMapName;
    private byte[] currentMapBytes;

    private String changingMapName;
    private byte[] changingMapBytes;
    private int changingMapDownloadedBytes;

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    public void start() {
        IntStream.range(0, MAX_PLAYERS_COUNT).forEach(freePlayerIds::add);

        try {
            try (ServerSocket serverSocket = new ServerSocket(ServerConfig.PORT)) {
                LOGGER.log(Level.INFO, "Server ready at port " + serverSocket.getLocalPort());
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    LOGGER.log(Level.INFO, "Got connection from " + clientSocket.getInetAddress().getHostAddress());
                    GameClient client = new GameClient(clientSocket, this);
                    clients.add(client);
                    new Thread(client).start();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(Message message, GameClient sender) {
        switch (message.getType()) {
            case PLAYER_CONNECTED,
                    PLAYER_CONNECTION_DENIAL,
                    GAME_STARTED,
                    PLAYER_CONNECTION_APPROVED -> { /* no-op */ }
            case PLAYER_CONNECTION_REQUEST -> {
                if (gameStarted) {
                    LOGGER.log(Level.INFO, "Denied player connection: game is running");
                    Message denialMessage = new PlayerConnectionDenialMessage(ConnectionDenialReason.GAME_RUNNING);
                    sendMessageSafely(denialMessage, sender);
                    sender.stop();
                    clients.remove(sender);
                } else if (freePlayerIds.isEmpty()) {
                    LOGGER.log(Level.INFO, "Denied player connection: room is full");
                    Message denialMessage = new PlayerConnectionDenialMessage(ConnectionDenialReason.ROOM_FULL);
                    sendMessageSafely(denialMessage, sender);
                    sender.stop();
                    clients.remove(sender);
                } else {
                    PlayerConnectionRequestMessage request = (PlayerConnectionRequestMessage) message;
                    String username = request.getUsername().strip();
                    if (!username.isEmpty() && username.length() <= 32) {
                        int id = freePlayerIds.pollFirst();
                        sender.setPlayer(new Player(id, username));
                        Message connectedMessage = new PlayerConnectedMessage(id, username);
                        for (GameClient client : clients) {
                            if (!client.equals(sender)) {
                                sendMessageSafely(connectedMessage, client);
                            }
                        }
                        Message connectionApprovedMessage = new PlayerConnectionApprovedMessage(
                                new ShallowPlayer(id, username),
                                clients.stream()
                                        .filter(client -> client.getPlayer() != null)
                                        .map(client -> client.getPlayer().toShallowPlayer())
                                        .toList()
                        );
                        sendMessageSafely(connectionApprovedMessage, sender);
                        if (currentMapName != null && currentMapBytes != null) {
                            Message mapMessage = new MapChangeMessage(currentMapBytes.length, currentMapName);
                            sendMessageSafely(mapMessage, sender);
                        }
                        for (int playerDownloadedId : clients.stream()
                                .filter(client -> client.getPlayer() != null)
                                .filter(client -> client.getPlayer().isDownloadedMap())
                                .map(client -> client.getPlayer().getId())
                                .toList()) {
                            Message downloadedMessage = new MapDownloadedMessage(playerDownloadedId);
                            sendMessageSafely(downloadedMessage, sender);
                        }

                        LOGGER.log(Level.INFO, "Connected player " + sender);
                    } else {
                        LOGGER.log(Level.INFO, "Denied player connection: illegal username");
                        Message denialMessage = new PlayerConnectionDenialMessage(ConnectionDenialReason.ILLEGAL_USERNAME);
                        sendMessageSafely(denialMessage, sender);
                        sender.stop();
                        clients.remove(sender);
                    }
                }
            }
            case PLAYER_DISCONNECTED -> {
                sender.stop();
                clients.remove(sender);
                if (sender.getPlayer() != null) {
                    freePlayerIds.add(sender.getPlayer().getId());
                    broadcastMessageSafely(message);
                    LOGGER.log(Level.INFO, "Player " + sender + " disconnected: " + message);

                    if (freePlayerIds.size() == MAX_PLAYERS_COUNT && gameStarted) {
                        gameStarted = false;
                        LOGGER.log(Level.INFO, "Game finished because all players left");
                    }
                } else {
                    LOGGER.log(Level.INFO, "Client disconnected");
                }
            }
            case MAP_CHANGE -> {
                MapChangeMessage mapChangeMessage = (MapChangeMessage) message;
                changingMapName = mapChangeMessage.getName();
                changingMapBytes = new byte[mapChangeMessage.getSize()];
                changingMapDownloadedBytes = 0;
                LOGGER.log(Level.INFO, "Changed map: " + mapChangeMessage);
            }
            case MAP_CHUNK -> {
                if (changingMapName != null && changingMapBytes != null) {
                    MapChunkMessage mapChunkMessage = (MapChunkMessage) message;
                    int mapChunkLength = mapChunkMessage.getMapChunk().length;
                    System.arraycopy(mapChunkMessage.getMapChunk(), 0, changingMapBytes, mapChunkMessage.getOffset(), mapChunkLength);
                    changingMapDownloadedBytes += mapChunkLength;
                    if (changingMapDownloadedBytes == changingMapBytes.length) {
                        currentMapName = changingMapName;
                        currentMapBytes = changingMapBytes;
                        clients.stream()
                                .filter(client -> client.getPlayer() != null)
                                .forEach(client -> client.getPlayer().setDownloadedMap(false));
                        sender.getPlayer().setDownloadedMap(true);

                        Message serverMapDownloadedMessage = new MapDownloadedMessage(null);
                        sendMessageSafely(serverMapDownloadedMessage, sender);

                        Message mapChangeMessage = new MapChangeMessage(currentMapBytes.length, currentMapName);
                        Message clientMapDownloadedMessage = new MapDownloadedMessage(sender.getPlayer().getId());
                        for (GameClient client : clients) {
                            if (!client.equals(sender)) {
                                sendMessageSafely(mapChangeMessage, client);
                                sendMessageSafely(clientMapDownloadedMessage, client);
                            }
                        }

                        LOGGER.log(Level.INFO, "Map downloaded");
                    }
                }
            }
            case MAP_CHUNK_REQUEST -> {
                if (currentMapName != null && currentMapBytes != null) {
                    MapChunkRequestMessage request = (MapChunkRequestMessage) message;
                    if (request.getOffset() < currentMapBytes.length) {
                        byte[] chunk = Arrays.copyOfRange(currentMapBytes, request.getOffset(), Math.min(currentMapBytes.length, request.getOffset() + ServerConfig.MAP_CHUNK_LENGTH));
                        MapChunkMessage chunkMessage = new MapChunkMessage(request.getOffset(), chunk);
                        sendMessageSafely(chunkMessage, sender);
                    }
                }
            }
            case MAP_DOWNLOADED -> {
                sender.getPlayer().setDownloadedMap(true);
                for (GameClient client : clients) {
                    if (!client.equals(sender)) {
                        sendMessageSafely(message, client);
                    }
                }
            }
            case PLAYER_READY -> {
                for (GameClient client : clients) {
                    if (!client.equals(sender)) {
                        sendMessageSafely(message, client);
                    }
                }
                if (!gameStarted) {
                    sender.getPlayer().setInGame(true);
                    LOGGER.log(Level.INFO, sender + " is ready");

                    boolean allPlayersReady = clients.stream()
                            .filter(client -> client.getPlayer() != null)
                            .allMatch(client -> client.getPlayer().isInGame());
                    if (allPlayersReady) {
                        gameStarted = true;
                        Message startedMessage = new GameStartedMessage();
                        broadcastMessageSafely(startedMessage);

                        LOGGER.log(Level.INFO, "Game started");
                    }
                } else {
                    sender.getPlayer().setInGame(false);
                    LOGGER.log(Level.INFO, sender + " finished");

                    boolean allPlayersFinished = clients.stream()
                            .filter(client -> client.getPlayer() != null)
                            .noneMatch(client -> client.getPlayer().isInGame());
                    if (allPlayersFinished) {
                        gameStarted = false;
                        LOGGER.log(Level.INFO, "Game finished");
                    }
                }
            }
            default -> {
                for (GameClient client : clients) {
                    if (!client.equals(sender)) {
                        sendMessageSafely(message, client);
                    }
                }
            }
        }
    }

    private void sendMessageSafely(Message message, GameClient client) {
        try {
            client.writeMessage(message);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unexpected exception when sending message", e);
            if (client.getPlayer() != null) {
                PlayerDisconnectedMessage disconnectedMessage = new PlayerDisconnectedMessage(client.getPlayer().getId(), DisconnectionReason.LOST_CONNECTION);
                handleMessage(disconnectedMessage, client);
            }
        }
    }

    private void broadcastMessageSafely(Message message) {
        for (GameClient client : clients) {
            sendMessageSafely(message, client);
        }
    }

    private static class GameClient implements Runnable {

        private final Socket socket;
        private final GameServer server;
        private Player player;

        private boolean isConnected = true;

        public GameClient(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }

        private void handleMessage(Message message) {
            if (message.getType() == MessageType.PLAYER_DISCONNECTED) {
                isConnected = false;
            }
            server.handleMessage(message, this);
        }

        @Override
        public void run() {
            try {
                while (isConnected) {
                    Message message = Message.createMessage(socket.getInputStream());
                    handleMessage(message);
                }
            } catch (IOException e) {
                if (player != null) {
                    Message message = new PlayerDisconnectedMessage(player.getId(), DisconnectionReason.LOST_CONNECTION);
                    server.handleMessage(message, this);
                }
            }
        }

        public void writeMessage(Message message) throws IOException {
            socket.getOutputStream().write(message.getRaw());
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }

        @Override
        public String toString() {
            return player.getUsername() + " (" + player.getId() + ")";
        }

        public void stop() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ignored) {}
            isConnected = false;
        }
    }
}
