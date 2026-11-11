package com.maketo.annonce.policy;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.Formule;
import com.maketo.utilisateur.entity.Utilisateur;

import java.time.Instant;

/**
 * Règles de publication d'une formule (pattern Strategy).
 * Ajouter une formule (ex. "Boutique") = ajouter une classe @Component, sans modifier le service.
 */
public interface PolitiquePublication {

    Formule formule();

    /** Cette politique s'applique-t-elle à ce vendeur ? */
    boolean sApplique(Utilisateur vendeur, Instant maintenant);

    /**
     * Lève une exception si le vendeur ne peut pas publier.
     *
     * @param annonceRenouvelee l'annonce renouvelée, ou null pour une nouvelle publication
     */
    void verifierDroit(Utilisateur vendeur, Annonce annonceRenouvelee, Instant maintenant);

    Instant dateExpiration(Instant maintenant);
}
