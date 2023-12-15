package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.nio.ByteBuffer;

public class MapDownloadedMessage extends Message {

    private final Integer playerId; // null means server

    public MapDownloadedMessage(Integer playerId) {
        super(MessageType.MAP_DOWNLOADED, new byte[0]);
        this.playerId = playerId;
        if (playerId != null) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(playerId);
            setData(buffer.array());
        }
    }

    // package-private
    MapDownloadedMessage(byte[] data) {
        super(MessageType.MAP_DOWNLOADED, data);
        if (data.length != 0) {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            playerId = buffer.getInt();
        } else {
            playerId = null;
        }
    }

    public Integer getPlayerId() {
        return playerId;
    }
}
