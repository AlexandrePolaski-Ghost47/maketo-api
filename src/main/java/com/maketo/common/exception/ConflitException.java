package com.maketo.common.exception;

/** Conflit avec une donnée existante (409). */
public class ConflitException extends MetierException {

    public ConflitException(String message) {
        super(message);
    }
}
