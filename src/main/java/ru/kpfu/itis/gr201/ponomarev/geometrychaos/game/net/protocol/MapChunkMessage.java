package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MapChunkMessage extends Message {

    private final int offset;
    private final byte[] mapChunk;

    public MapChunkMessage(int offset, byte[] mapChunk) {
        super(MessageType.MAP_CHUNK, new byte[0]);
        this.offset = offset;
        this.mapChunk = Arrays.copyOf(mapChunk, mapChunk.length);
        ByteBuffer buffer = ByteBuffer.allocate(4 + mapChunk.length);
        buffer.putInt(offset);
        buffer.put(mapChunk);
        setData(buffer.array());
    }

    // package-private
    MapChunkMessage(byte[] data) {
        super(MessageType.MAP_CHUNK, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        offset = buffer.getInt();
        mapChunk = new byte[getData().length - 4];
        buffer.get(mapChunk);
    }

    public int getOffset() {
        return offset;
    }

    public byte[] getMapChunk() {
        return mapChunk;
    }

    @Override
    public String toString() {
        return "byte " + getOffset() + ", " + getMapChunk().length + " bytes";
    }
}
