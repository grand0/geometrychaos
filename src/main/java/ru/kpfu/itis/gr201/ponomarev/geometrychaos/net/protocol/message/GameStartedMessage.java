package ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.message;

public class GameStartedMessage extends Message {
    
    public GameStartedMessage() {
        super(MessageType.GAME_STARTED, new byte[0]);
    }
    
    // package-private
    GameStartedMessage(byte[] data) {
        super(MessageType.GAME_STARTED, data);
    }
}
