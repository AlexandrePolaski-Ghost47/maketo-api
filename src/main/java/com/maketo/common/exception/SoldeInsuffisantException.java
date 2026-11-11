package com.maketo.common.exception;

public class SoldeInsuffisantException extends MetierException {

    public SoldeInsuffisantException(int solde, int montantDemande) {
        super("Solde insuffisant : " + solde + " F disponibles, " + montantDemande + " F demandés");
    }
}
