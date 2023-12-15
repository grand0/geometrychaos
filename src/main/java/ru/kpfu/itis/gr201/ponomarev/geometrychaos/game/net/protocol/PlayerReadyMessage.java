package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.nio.ByteBuffer;

public class PlayerReadyMessage extends Message {
    
    private static final int DATA_LENGTH = 4;
    
    private final int playerId;
    
    public PlayerReadyMessage(int playerId) {
        super(MessageType.PLAYER_READY, new byte[0]);
        this.playerId = playerId;
        ByteBuffer buffer = ByteBuffer.allocate(DATA_LENGTH);
        buffer.putInt(playerId);
        setData(buffer.array());
    }
    
    // package-private
    PlayerReadyMessage(byte[] data) {
        super(MessageType.PLAYER_READY, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        playerId = buffer.getInt();
    }

    public int getPlayerId() {
        return playerId;
    }
}
