package com.maketo.common.exception;

public class TropDeTentativesException extends RuntimeException {

    public TropDeTentativesException(long minutes) {
        super("Trop de tentatives. Réessaie dans " + minutes + " minute(s)");
    }
}
