package com.maketo.common.exception;

/** Ressource introuvable (404). */
public class RessourceIntrouvableException extends MetierException {

    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
