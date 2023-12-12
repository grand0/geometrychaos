package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.exception;

public class LevelLoadException extends Exception {
    public LevelLoadException() {
        super();
    }

    public LevelLoadException(String message) {
        super(message);
    }

    public LevelLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LevelLoadException(Throwable cause) {
        super(cause);
    }

    protected LevelLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
