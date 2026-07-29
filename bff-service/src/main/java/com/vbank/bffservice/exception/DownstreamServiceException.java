package com.vbank.bffservice.exception;

/**
 * Deliberately generic across "connection refused," "timeout," and
 * "downstream returned 500" - the BFF consumer doesn't need to know
 * WHICH failure mode happened, only that dashboard assembly failed.
 * The specific cause still goes to logs (see GlobalExceptionHandler),
 * just not to the API response - matches the spec's documented 500
 * example exactly.
 */
public class DownstreamServiceException extends RuntimeException {
    public DownstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
