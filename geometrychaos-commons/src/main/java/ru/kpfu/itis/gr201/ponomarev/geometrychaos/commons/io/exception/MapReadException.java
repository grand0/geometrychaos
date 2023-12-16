package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.exception;

import java.io.IOException;

public class MapReadException extends IOException {

    public MapReadException() {
        super();
    }

    public MapReadException(String message) {
        super(message);
    }

    public MapReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public MapReadException(Throwable cause) {
        super(cause);
    }
}
