package com.maketo.credit.dto;

import com.maketo.credit.entity.StatutPaiement;

import java.time.Instant;

/** Suivi d'une recharge par l'étudiant (le front interroge cet état toutes les 3 à 5 secondes). */
public record PaiementResponse(
        String reference,
        int montant,
        String operateur,
        StatutPaiement statut,
        String message,
        Instant dateCreation
) {
}
