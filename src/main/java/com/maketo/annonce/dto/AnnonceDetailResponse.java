package com.maketo.annonce.dto;

import com.maketo.annonce.entity.EtatArticle;
import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.annonce.entity.TypeAnnonce;
import com.maketo.categorie.dto.CategorieResponse;

import java.time.Instant;
import java.util.List;

public record AnnonceDetailResponse(
        Long id,
        TypeAnnonce type,
        String titre,
        String description,
        Integer prix,
        boolean prixADebattre,
        EtatArticle etat,
        String lieu,
        CategorieResponse categorie,
        List<PhotoResponse> photos,
        VendeurResponse vendeur,
        StatutAnnonce statut,
        boolean boostee,
        int nbVues,
        Instant dateCreation,
        Instant dateExpiration
) {
}
