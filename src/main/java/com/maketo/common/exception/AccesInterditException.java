package com.maketo.common.exception;

/** L'utilisateur est connecté mais n'a pas le droit d'agir sur cette ressource (ex. annonce d'un autre). */
public class AccesInterditException extends RuntimeException {

    public AccesInterditException(String message) {
        super(message);
    }
}
