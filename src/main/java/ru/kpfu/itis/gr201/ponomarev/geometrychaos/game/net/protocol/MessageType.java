package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.util.function.Function;

public enum MessageType {
    PLAYER_POSITION_UPDATE(1, PlayerPositionUpdateMessage::new),
    PLAYER_DASH(2, PlayerDashMessage::new),
    PLAYER_HIT(3, PlayerHitMessage::new),
    PLAYER_CONNECTION_REQUEST(4, PlayerConnectionRequestMessage::new),
    PLAYER_CONNECTED(5, PlayerConnectedMessage::new),
    PLAYER_CONNECTION_DENIAL(6, PlayerConnectionDenialMessage::new),
    PLAYER_DISCONNECTED(7, PlayerDisconnectedMessage::new),
    MAP_CHANGE(8, MapChangeMessage::new),
    MAP_CHUNK(9, MapChunkMessage::new),
    MAP_CHUNK_REQUEST(10, MapChunkRequestMessage::new),
    MAP_DOWNLOADED(11, MapDownloadedMessage::new),
//    GAME_STARTING(12, GameStartingMessage::new),
    PLAYER_READY(13, PlayerReadyMessage::new),
    GAME_STARTED(14, GameStartedMessage::new),
    PLAYER_CONNECTION_APPROVED(15, PlayerConnectionApprovedMessage::new),
    ;

    private final int id;
    private final Function<byte[], Message> instantiator;

    MessageType(int id, Function<byte[], Message> instantiator) {
        this.id = id;
        this.instantiator = instantiator;
    }

    public static MessageType getById(int id) {
        for (MessageType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    // package-private
    Message instantiate(byte[] data) {
        return instantiator.apply(data);
    }

    public int getId() {
        return id;
    }
}
