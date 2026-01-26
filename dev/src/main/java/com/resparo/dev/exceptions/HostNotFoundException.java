package com.resparo.dev.exceptions;

public class HostNotFoundException extends Exception {
    public HostNotFoundException() {
        super();
    }

    public HostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public HostNotFoundException(String message) {
        super(message);
    }
}
