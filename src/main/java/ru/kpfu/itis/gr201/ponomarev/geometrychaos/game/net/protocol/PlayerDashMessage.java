package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.nio.ByteBuffer;

public class PlayerDashMessage extends Message {

    private final static int DATA_LENGTH = 4 + 8 * 2;

    private final int playerId;
    private final double velocityX;
    private final double velocityY;

    public PlayerDashMessage(int playerId, double velocityX, double velocityY) {
        super(MessageType.PLAYER_DASH, new byte[DATA_LENGTH]);
        this.playerId = playerId;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        ByteBuffer buffer = ByteBuffer.allocate(DATA_LENGTH);
        buffer.putInt(playerId);
        buffer.putDouble(velocityX);
        buffer.putDouble(velocityY);
        setData(buffer.array());
    }

    // package-private
    PlayerDashMessage(byte[] data) {
        super(MessageType.PLAYER_DASH, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        playerId = buffer.getInt();
        velocityX = buffer.getDouble();
        velocityY = buffer.getDouble();
    }

    public int getPlayerId() {
        return playerId;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }
}
