package com.maketo.annonce.dto;

import com.maketo.annonce.entity.Formule;
import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.annonce.entity.TypeAnnonce;

import java.time.Instant;

/** Annonce vue par son propriétaire, avec ses statistiques. */
public record MonAnnonceResponse(
        Long id,
        TypeAnnonce type,
        String titre,
        Integer prix,
        boolean prixADebattre,
        String photoPrincipale,
        StatutAnnonce statut,
        Formule formule,
        boolean visible,
        boolean boostee,
        boolean renouvelable,
        int nbVues,
        int nbContacts,
        int nbRenouvellements,
        Instant dateCreation,
        Instant dateExpiration
) {
}
