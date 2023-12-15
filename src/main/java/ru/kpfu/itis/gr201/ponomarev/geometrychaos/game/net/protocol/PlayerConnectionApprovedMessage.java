package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PlayerConnectionApprovedMessage extends Message {

    private final ShallowPlayer thisPlayer;
    private final List<ShallowPlayer> players;

    public PlayerConnectionApprovedMessage(ShallowPlayer thisPlayer, List<ShallowPlayer> players) {
        super(MessageType.PLAYER_CONNECTION_APPROVED, new byte[0]);
        this.thisPlayer = thisPlayer;
        this.players = players;
        byte[][] namesBytes = new byte[players.size() + 1][];
        int namesBytesSize = 0;
        for (int i = 0; i <= players.size(); i++) {
            if (i == 0) {
                namesBytes[i] = thisPlayer.username().getBytes(StandardCharsets.UTF_8);
            } else {
                ShallowPlayer player = players.get(i - 1);
                namesBytes[i] = player.username().getBytes(StandardCharsets.UTF_8);
            }
            namesBytesSize += namesBytes[i].length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(4 * (players.size() + 1) + namesBytesSize + (players.size() + 1)); // ids + names + dividers
        for (int i = 0; i <= players.size(); i++) {
            if (i == 0) {
                buffer.putInt(thisPlayer.playerId());
            } else {
                buffer.putInt(players.get(i - 1).playerId());
            }
            buffer.put(namesBytes[i]);
            buffer.put((byte) 0);
        }
        setData(buffer.array());
    }

    public PlayerConnectionApprovedMessage(byte[] data) {
        super(MessageType.PLAYER_CONNECTION_APPROVED, data);
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        players = new ArrayList<>();
        while (dataBuffer.hasRemaining()) {
            int playerId = dataBuffer.getInt();
            ByteArrayOutputStream usernameBuffer = new ByteArrayOutputStream();
            byte b;
            while ((b = dataBuffer.get()) != 0) {
                usernameBuffer.write(b);
            }
            String username = usernameBuffer.toString(StandardCharsets.UTF_8);
            players.add(new ShallowPlayer(playerId, username));
        }
        thisPlayer = players.get(0);
        players.remove(0);
    }

    public ShallowPlayer getThisPlayer() {
        return thisPlayer;
    }

    public List<ShallowPlayer> getPlayers() {
        return players;
    }
}
