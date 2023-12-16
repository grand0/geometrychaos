package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game;

public enum PlayerState {
    IN_ROOM("In room"),
    DOWNLOADING("Downloading"),
    READY("Ready"),
    ;

    private final String message;

    PlayerState(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
