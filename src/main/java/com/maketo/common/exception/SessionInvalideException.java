package com.maketo.common.exception;

public class SessionInvalideException extends RuntimeException {

    public SessionInvalideException() {
        super("Session expirée, reconnecte-toi");
    }
}
