package org.sputnikdev.bluetooth.manager.transport.tinyb;

public class TinyBException extends RuntimeException {
    public TinyBException() {
        super();
    }

    public TinyBException(String message) {
        super(message);
    }

    public TinyBException(String message, Throwable cause) {
        super(message, cause);
    }
}
