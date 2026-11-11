package com.maketo.common.exception;

/**
 * Violation d'une règle métier (renvoyée au client en HTTP 400/409).
 */
public class MetierException extends RuntimeException {

    public MetierException(String message) {
        super(message);
    }
}
