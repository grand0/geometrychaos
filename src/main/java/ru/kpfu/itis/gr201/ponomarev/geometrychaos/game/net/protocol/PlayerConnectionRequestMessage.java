package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.nio.charset.StandardCharsets;

public class PlayerConnectionRequestMessage extends Message {

    private final String username;

    public PlayerConnectionRequestMessage(String username) {
        super(MessageType.PLAYER_CONNECTION_REQUEST, new byte[0]);
        this.username = username;
        byte[] bytes = username.getBytes(StandardCharsets.UTF_8);
        setData(bytes);
    }

    // package-private
    PlayerConnectionRequestMessage(byte[] data) {
        super(MessageType.PLAYER_CONNECTION_REQUEST, data);
        username = new String(getData(), StandardCharsets.UTF_8);
    }

    public String getUsername() {
        return username;
    }
}
