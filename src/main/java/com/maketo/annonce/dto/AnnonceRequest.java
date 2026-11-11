package com.maketo.annonce.dto;

import com.maketo.annonce.entity.EtatArticle;
import com.maketo.annonce.entity.TypeAnnonce;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Création et modification d'une annonce.
 * ARTICLE : titre, prix (ou à débattre) et état obligatoires.
 * MESSAGE : seul le message (description) est obligatoire ; le titre est déduit s'il est vide.
 * Ces règles par type sont vérifiées dans l'entité Annonce.
 */
public record AnnonceRequest(

        /** ARTICLE par défaut. Ignoré en modification (le type ne change pas). */
        TypeAnnonce type,

        @NotNull(message = "Choisis une catégorie")
        Long categorieId,

        @Size(max = 100, message = "100 caractères maximum")
        String titre,

        @Size(max = 2000, message = "2000 caractères maximum")
        String description,

        @PositiveOrZero(message = "Le prix ne peut pas être négatif")
        @Max(value = 100_000_000, message = "Prix trop élevé")
        Integer prix,

        boolean prixADebattre,

        EtatArticle etat,

        @Size(max = 100, message = "100 caractères maximum")
        String lieu
) {
}
