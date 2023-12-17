package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message;

import java.nio.ByteBuffer;

public class PlayerUpdateMessage extends Message {

    private final static int DATA_LENGTH = 4 + 8 * 4 + 4;

    private final int playerId;
    private final double positionX;
    private final double positionY;
    private final double velocityX;
    private final double velocityY;
    private final int healthPoints;

    public PlayerUpdateMessage(int playerId, double positionX, double positionY, double velocityX, double velocityY, int healthPoints) {
        super(MessageType.PLAYER_UPDATE, new byte[DATA_LENGTH]);
        this.playerId = playerId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.healthPoints = healthPoints;
        ByteBuffer buffer = ByteBuffer.allocate(DATA_LENGTH);
        buffer.putInt(playerId);
        buffer.putDouble(positionX);
        buffer.putDouble(positionY);
        buffer.putDouble(velocityX);
        buffer.putDouble(velocityY);
        buffer.putInt(healthPoints);
        setData(buffer.array());
    }

    // package-private
    PlayerUpdateMessage(byte[] data) {
        super(MessageType.PLAYER_UPDATE, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        playerId = buffer.getInt();
        positionX = buffer.getDouble();
        positionY = buffer.getDouble();
        velocityX = buffer.getDouble();
        velocityY = buffer.getDouble();
        healthPoints = buffer.getInt();
    }

    public int getPlayerId() {
        return playerId;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public int getHealthPoints() {
        return healthPoints;
    }
}
