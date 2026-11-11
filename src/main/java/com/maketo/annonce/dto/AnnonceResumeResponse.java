package com.maketo.annonce.dto;

import com.maketo.annonce.entity.EtatArticle;
import com.maketo.annonce.entity.TypeAnnonce;

import java.time.Instant;

/** Carte d'annonce dans les listes publiques. @param extrait début du message (petites annonces uniquement) */
public record AnnonceResumeResponse(
        Long id,
        TypeAnnonce type,
        String titre,
        String extrait,
        Integer prix,
        boolean prixADebattre,
        EtatArticle etat,
        String lieu,
        String categorieSlug,
        String photoPrincipale,
        boolean boostee,
        boolean vendeurPro,
        Instant dateCreation
) {
}
