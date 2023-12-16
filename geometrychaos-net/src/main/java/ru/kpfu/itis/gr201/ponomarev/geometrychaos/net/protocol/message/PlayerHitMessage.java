package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message;

import java.nio.ByteBuffer;

public class PlayerHitMessage extends Message {

    private final static int DATA_LENGTH = 4 * 2;

    private final int playerId;
    private final int healthPoints;

    public PlayerHitMessage(int playerId, int healthPoints) {
        super(MessageType.PLAYER_HIT, new byte[DATA_LENGTH]);
        this.playerId = playerId;
        this.healthPoints = healthPoints;
        ByteBuffer buffer = ByteBuffer.allocate(DATA_LENGTH);
        buffer.putInt(playerId);
        buffer.putInt(healthPoints);
        setData(buffer.array());
    }

    // package-private
    PlayerHitMessage(byte[] data) {
        super(MessageType.PLAYER_HIT, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        playerId = buffer.getInt();
        healthPoints = buffer.getInt();
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getHealthPoints() {
        return healthPoints;
    }
}
