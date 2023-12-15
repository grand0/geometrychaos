package ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.io.exception;

import java.io.IOException;

public class LevelReadException extends IOException {
    public LevelReadException() {
        super();
    }

    public LevelReadException(String message) {
        super(message);
    }

    public LevelReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LevelReadException(Throwable cause) {
        super(cause);
    }
}
