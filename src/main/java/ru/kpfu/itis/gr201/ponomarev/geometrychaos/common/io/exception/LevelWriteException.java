package ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.io.exception;

import java.io.IOException;

public class LevelWriteException extends IOException {
    public LevelWriteException() {
        super();
    }

    public LevelWriteException(String message) {
        super(message);
    }

    public LevelWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public LevelWriteException(Throwable cause) {
        super(cause);
    }
}
