package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.net.protocol;

public enum ConnectionDenialReason {
    UNKNOWN(0, "Unknown error"),
    ROOM_FULL(1, "Room is full"),
    ILLEGAL_USERNAME(2, "Illegal username"),
    GAME_RUNNING(3, "Game is already running")
    ;

    private final int id;
    private final String message;

    ConnectionDenialReason(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public static ConnectionDenialReason getById(int id) {
        for (ConnectionDenialReason reason : values()) {
            if (reason.id == id) {
                return reason;
            }
        }
        return UNKNOWN;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
