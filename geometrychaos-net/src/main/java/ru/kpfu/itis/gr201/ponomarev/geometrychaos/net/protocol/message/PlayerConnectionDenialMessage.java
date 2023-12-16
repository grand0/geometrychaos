package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.ConnectionDenialReason;

import java.nio.ByteBuffer;

public class PlayerConnectionDenialMessage extends Message {

    private final static int DATA_LENGTH = 4;

    private final ConnectionDenialReason denialReason;

    public PlayerConnectionDenialMessage(ConnectionDenialReason denialReason) {
        super(MessageType.PLAYER_CONNECTION_DENIAL, new byte[0]);
        this.denialReason = denialReason;
        ByteBuffer buffer = ByteBuffer.allocate(DATA_LENGTH);
        buffer.putInt(denialReason.getId());
        setData(buffer.array());
    }

    // package-private
    PlayerConnectionDenialMessage(byte[] data) {
        super(MessageType.PLAYER_CONNECTION_DENIAL, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        denialReason = ConnectionDenialReason.getById(buffer.getInt());
    }

    public ConnectionDenialReason getDenialReason() {
        return denialReason;
    }
}
