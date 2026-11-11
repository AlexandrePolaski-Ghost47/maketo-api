package com.maketo.common.exception;

/** Message volontairement vague : on ne révèle pas si le numéro existe. */
public class IdentifiantsInvalidesException extends RuntimeException {

    public IdentifiantsInvalidesException() {
        super("Numéro ou mot de passe incorrect");
    }
}
