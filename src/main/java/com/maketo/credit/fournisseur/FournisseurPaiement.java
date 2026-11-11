package com.maketo.credit.fournisseur;

import java.util.List;

/**
 * Abstraction d'un agrégateur Mobile Money. Changer d'agrégateur = une nouvelle implémentation
 * + une ligne de configuration (maketo.paiement.fournisseur), sans toucher au reste de l'appli.
 */
public interface FournisseurPaiement {

    /** Code court enregistré avec chaque paiement (ex. "CTPAY"). */
    String code();

    List<OperateurPaiement> operateurs();

    /** Lance le paiement : le client reçoit une demande de validation (PIN) sur son téléphone. */
    ResultatPaiement initier(DemandePaiement demande);

    /**
     * Statut réel du paiement chez le fournisseur. Ne doit jamais renvoyer ECHOUE sur une simple
     * erreur réseau : en cas de doute, renvoyer EN_ATTENTE (on revérifiera plus tard).
     */
    ResultatPaiement statut(String processCode);
}
