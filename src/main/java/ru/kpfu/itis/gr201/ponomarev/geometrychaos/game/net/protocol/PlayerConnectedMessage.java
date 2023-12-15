package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PlayerConnectedMessage extends Message {

    private final int playerId;
    private final String username;

    public PlayerConnectedMessage(int playerId, String username) {
        super(MessageType.PLAYER_CONNECTED, new byte[0]);
        this.playerId = playerId;
        this.username = username;
        byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + usernameBytes.length);
        buffer.putInt(playerId);
        buffer.put(usernameBytes);
        setData(buffer.array());
    }

    // package-private
    PlayerConnectedMessage(byte[] data) {
        super(MessageType.PLAYER_CONNECTED, data);
        ByteBuffer buffer = ByteBuffer.wrap(getData());
        playerId = buffer.getInt();
        byte[] usernameBytes = new byte[getData().length - 4];
        buffer.get(usernameBytes);
        username = new String(usernameBytes, StandardCharsets.UTF_8);
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return username;
    }
}
