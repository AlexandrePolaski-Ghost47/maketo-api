package com.maketo.signalement.dto;

import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.signalement.entity.MotifSignalement;
import com.maketo.signalement.entity.StatutSignalement;

import java.time.Instant;

/** Vue admin d'un signalement. */
public record SignalementResponse(
        Long id,
        Long annonceId,
        String annonceTitre,
        StatutAnnonce annonceStatut,
        MotifSignalement motif,
        String commentaire,
        String auteur,
        StatutSignalement statut,
        Instant dateCreation
) {
}
