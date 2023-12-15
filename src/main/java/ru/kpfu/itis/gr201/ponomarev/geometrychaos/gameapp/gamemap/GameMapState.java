package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap;

public enum GameMapState {
    DOWNLOADING("Downloading"),
    UPLOADING("Uploading"),
    FINISHED("Finished"),
    ;

    private final String message;

    GameMapState(String message) {
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
