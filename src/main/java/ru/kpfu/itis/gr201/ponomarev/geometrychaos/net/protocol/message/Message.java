package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

// [message type (4), data length (4), data (x)]
public abstract class Message {

    private final MessageType type;
    private byte[] data;

    public Message(MessageType type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public static Message createMessage(InputStream in) throws IOException {
        byte[] typeBytes = in.readNBytes(4);
        MessageType type = MessageType.getById(ByteBuffer.wrap(typeBytes).getInt());
        if (type == null) {
            throw new IOException("Unknown message type");
        }
        byte[] dataLengthBytes = in.readNBytes(4);
        int dataLength = ByteBuffer.wrap(dataLengthBytes).getInt();
        if (dataLength < 0) {
            throw new IOException("Invalid data length");
        }
        byte[] data = in.readNBytes(dataLength);
        return type.instantiate(data);
    }

    public byte[] getRaw() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + data.length); // type + data length + data
        buffer.putInt(getType().getId());
        buffer.putInt(data.length);
        buffer.put(data);
        return buffer.array();
    }

    public MessageType getType() {
        return type;
    }

    protected void setData(byte[] data) {
        this.data = data;
    }

    protected byte[] getData() {
        return data;
    }
}
