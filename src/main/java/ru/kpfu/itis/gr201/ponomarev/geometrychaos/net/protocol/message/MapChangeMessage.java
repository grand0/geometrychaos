package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MapChangeMessage extends Message {

    private final int size;
    private final String name;

    public MapChangeMessage(int size, String name) {
        super(MessageType.MAP_CHANGE, new byte[0]);
        this.size = size;
        this.name = name;
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + nameBytes.length);
        buffer.putInt(size);
        buffer.put(nameBytes);
        setData(buffer.array());
    }

    // package-private
    MapChangeMessage(byte[] data) {
        super(MessageType.MAP_CHANGE, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        size = buffer.getInt();
        byte[] nameBytes = new byte[getData().length - 4];
        buffer.get(nameBytes);
        name = new String(nameBytes, StandardCharsets.UTF_8);
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + " (" + getSize() + " bytes)";
    }
}
