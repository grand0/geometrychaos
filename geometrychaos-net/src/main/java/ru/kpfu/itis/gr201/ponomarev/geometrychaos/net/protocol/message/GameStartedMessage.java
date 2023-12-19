package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message;

import java.nio.ByteBuffer;

public class GameStartedMessage extends Message {

    private final long seed;
    
    public GameStartedMessage(long seed) {
        super(MessageType.GAME_STARTED, new byte[0]);
        this.seed = seed;
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(seed);
        setData(buffer.array());
    }
    
    // package-private
    GameStartedMessage(byte[] data) {
        super(MessageType.GAME_STARTED, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        seed = buffer.getLong();
    }

    public long getSeed() {
        return seed;
    }
}
