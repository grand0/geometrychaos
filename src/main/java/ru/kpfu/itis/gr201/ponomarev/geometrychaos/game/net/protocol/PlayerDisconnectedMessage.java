package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.nio.ByteBuffer;

public class PlayerDisconnectedMessage extends Message {

    private final static int DATA_LENGTH = 2 * 4;

    private final int playerId;
    private final DisconnectionReason reason;

    public PlayerDisconnectedMessage(int playerId, DisconnectionReason reason) {
        super(MessageType.PLAYER_DISCONNECTED, new byte[0]);
        this.playerId = playerId;
        this.reason = reason;
        ByteBuffer buffer = ByteBuffer.allocate(DATA_LENGTH);
        buffer.putInt(playerId);
        buffer.putInt(reason.getId());
        setData(buffer.array());
    }

    // package-private
    PlayerDisconnectedMessage(byte[] data) {
        super(MessageType.PLAYER_DISCONNECTED, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        playerId = buffer.getInt();
        reason = DisconnectionReason.getById(buffer.getInt());
    }

    public int getPlayerId() {
        return playerId;
    }

    public DisconnectionReason getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return getReason().getMessage();
    }
}
