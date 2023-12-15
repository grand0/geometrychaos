package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.server;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol.*;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GameServer {

    private static final int MAX_PLAYERS_COUNT = 4;
    private static final Logger LOGGER = System.getLogger(GameServer.class.getName());

    private final List<GameClient> clients = new LinkedList<>();

    private int playerIdCounter = 0;
    private final Set<Integer> playersIds = new HashSet<>();

    private int playersReady = 0;
//    private boolean gameRunning = false;
    private boolean gameStarted = false;

    private String currentMapName;
    private byte[] currentMapBytes;

    private String changingMapName;
    private byte[] changingMapBytes;
    private int changingMapDownloadedBytes;

    private final Set<Integer> playersDownloadedMapIds = new HashSet<>(); // TODO: replace all sets of ids with one list of players

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    public void start() {
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
                } else if (playersIds.size() >= MAX_PLAYERS_COUNT) {
                    LOGGER.log(Level.INFO, "Denied player connection: room is full");
                    Message denialMessage = new PlayerConnectionDenialMessage(ConnectionDenialReason.ROOM_FULL);
                    sendMessageSafely(denialMessage, sender);
                    sender.stop();
                    clients.remove(sender);
                } else {
                    PlayerConnectionRequestMessage request = (PlayerConnectionRequestMessage) message;
                    String username = request.getUsername().strip();
                    if (!username.isEmpty() && username.length() <= 32) {
                        int id = playerIdCounter++;
                        playersIds.add(id);
                        sender.setPlayerId(id);
                        sender.setUsername(username);
                        Message connectedMessage = new PlayerConnectedMessage(id, username);
                        for (GameClient client : clients) {
                            if (!client.equals(sender)) {
                                sendMessageSafely(connectedMessage, client);
                            }
                        }
                        Message connectionApprovedMessage = new PlayerConnectionApprovedMessage(
                                new ShallowPlayer(id, username),
                                clients.stream()
                                        .filter(client -> client.getPlayerId() != null && client.getUsername() != null)
                                        .map(client -> new ShallowPlayer(client.getPlayerId(), client.getUsername()))
                                        .toList()
                        );
                        sendMessageSafely(connectionApprovedMessage, sender);
                        if (currentMapName != null && currentMapBytes != null) {
                            Message mapMessage = new MapChangeMessage(currentMapBytes.length, currentMapName);
                            sendMessageSafely(mapMessage, sender);
                        }
                        for (int playerDownloadedId : playersDownloadedMapIds) {
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
                if (sender.getPlayerId() != null) {
                    playersIds.remove(sender.getPlayerId());
                    broadcastMessageSafely(message);
                    LOGGER.log(Level.INFO, "Player " + sender + " disconnected: " + message);
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
//                    LOGGER.log(Level.INFO, "Got chunk: " + mapChunkMessage);
                    changingMapDownloadedBytes += mapChunkLength;
                    if (changingMapDownloadedBytes == changingMapBytes.length) {
                        currentMapName = changingMapName;
                        currentMapBytes = changingMapBytes;
                        playersDownloadedMapIds.clear();
                        playersDownloadedMapIds.add(sender.getPlayerId());

                        Message serverMapDownloadedMessage = new MapDownloadedMessage(null);
                        sendMessageSafely(serverMapDownloadedMessage, sender);

                        Message mapChangeMessage = new MapChangeMessage(currentMapBytes.length, currentMapName);
                        Message clientMapDownloadedMessage = new MapDownloadedMessage(sender.getPlayerId());
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

//                        LOGGER.log(Level.INFO, "Sent chunk to " + sender + ": " + chunkMessage);
                    }
                }
            }
            case MAP_DOWNLOADED -> {
                playersDownloadedMapIds.add(sender.getPlayerId());
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
                    playersReady++;
                    LOGGER.log(Level.INFO, sender + " is ready");

                    if (playersReady == playersIds.size()) {
                        gameStarted = true;
                        Message startedMessage = new GameStartedMessage();
                        broadcastMessageSafely(startedMessage);

                        LOGGER.log(Level.INFO, "Game started");
                    }
                } else {
                    playersReady--;
                    LOGGER.log(Level.INFO, sender + " finished");

                    if (playersReady == 0) {
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
            PlayerDisconnectedMessage disconnectedMessage = new PlayerDisconnectedMessage(client.playerId, DisconnectionReason.LOST_CONNECTION);
            handleMessage(disconnectedMessage, client);
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
        private Integer playerId;
        private String username;

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
                Message message = new PlayerDisconnectedMessage(playerId, DisconnectionReason.LOST_CONNECTION);
                server.handleMessage(message, this);
            }
        }

        public void writeMessage(Message message) throws IOException {
            socket.getOutputStream().write(message.getRaw());
        }

        public Integer getPlayerId() {
            return playerId;
        }

        public void setPlayerId(int playerId) {
            this.playerId = playerId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return username + " (" + playerId + ")";
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
