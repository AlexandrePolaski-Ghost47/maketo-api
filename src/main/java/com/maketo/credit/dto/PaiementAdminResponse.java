package com.maketo.credit.dto;

import com.maketo.credit.entity.StatutPaiement;

import java.time.Instant;

public record PaiementAdminResponse(
        Long id,
        String reference,
        String processCode,
        String fournisseur,
        Long utilisateurId,
        String utilisateur,
        String operateur,
        String telephone,
        int montant,
        Integer frais,
        StatutPaiement statut,
        String messageErreur,
        Instant dateCreation,
        Instant dateFinalisation
) {
}
