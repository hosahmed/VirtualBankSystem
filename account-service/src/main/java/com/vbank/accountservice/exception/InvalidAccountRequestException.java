package com.vbank.accountservice.exception;

public class InvalidAccountRequestException extends RuntimeException {
    public InvalidAccountRequestException(String message) {
        super(message);
    }
}
