package ru.kpfu.itis.gr201.ponomarev.bheditor.exception;

public class LevelSaveException extends Exception {
    public LevelSaveException() {
        super();
    }

    public LevelSaveException(String message) {
        super(message);
    }

    public LevelSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public LevelSaveException(Throwable cause) {
        super(cause);
    }

    protected LevelSaveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
