package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

public enum DisconnectionReason {
    LEFT(1, "%s left room"),
    LOST_CONNECTION(2, "%s lost connection"),
    ;

    private final int id;
    private final String message;

    DisconnectionReason(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public static DisconnectionReason getById(int id) {
        for (DisconnectionReason reason : values()) {
            if (reason.id == id) {
                return reason;
            }
        }
        return LOST_CONNECTION;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
