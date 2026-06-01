package com.innowise.authservice.exception;

public class BadIncomingDataException extends RuntimeException {
    public BadIncomingDataException(String message) {
        super(message);
    }
}
