package com.maketo.credit.fournisseur;

/** Réponse normalisée d'un fournisseur (initiation ou vérification de statut). */
public record ResultatPaiement(String processCode, EtatPaiementFournisseur etat, Integer frais, String message) {

    public static ResultatPaiement echec(String message) {
        return new ResultatPaiement(null, EtatPaiementFournisseur.ECHOUE, null, message);
    }

    public static ResultatPaiement inconnu(String processCode, String message) {
        return new ResultatPaiement(processCode, EtatPaiementFournisseur.EN_ATTENTE, null, message);
    }
}
