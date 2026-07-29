package com.vbank.loggingservice.exception;

public class MalformedLogMessageException extends RuntimeException {
    public MalformedLogMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
