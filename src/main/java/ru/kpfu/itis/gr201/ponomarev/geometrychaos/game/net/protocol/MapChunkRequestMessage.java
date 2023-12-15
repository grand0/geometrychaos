package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.nio.ByteBuffer;

public class MapChunkRequestMessage extends Message {

    private static final int DATA_LENGTH = 4;

    private final int offset;

    public MapChunkRequestMessage(int offset) {
        super(MessageType.MAP_CHUNK_REQUEST, new byte[0]);
        this.offset = offset;
        ByteBuffer buffer = ByteBuffer.allocate(DATA_LENGTH);
        buffer.putInt(offset);
        setData(buffer.array());
    }

    // package-private
    MapChunkRequestMessage(byte[] data) {
        super(MessageType.MAP_CHUNK_REQUEST, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        offset = buffer.getInt();
    }

    public int getOffset() {
        return offset;
    }
}
