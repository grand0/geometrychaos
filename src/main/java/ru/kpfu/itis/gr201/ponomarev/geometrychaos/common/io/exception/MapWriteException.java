package ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.io.exception;

import java.io.IOException;

public class MapWriteException extends IOException {

    public MapWriteException() {
        super();
    }

    public MapWriteException(String message) {
        super(message);
    }

    public MapWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapWriteException(Throwable cause) {
        super(cause);
    }
}
