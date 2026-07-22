package com.vbank.bffservice.exception;

public class UpstreamUserNotFoundException extends RuntimeException {
    public UpstreamUserNotFoundException(String message) {
        super(message);
    }
}
