package com.maketo.annonce.service;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.annonce.repository.AnnonceRepository;
import com.maketo.common.exception.AccesInterditException;
import com.maketo.common.exception.RessourceIntrouvableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Chargement d'annonce + contrôle de propriété, partagé par les services du module annonce. */
@Component
@RequiredArgsConstructor
public class AnnonceChargeur {

    private final AnnonceRepository annonceRepository;

    public Annonce charger(Long annonceId) {
        return annonceRepository.findDetailById(annonceId)
                .filter(a -> a.getStatut() != StatutAnnonce.SUPPRIMEE)
                .orElseThrow(() -> new RessourceIntrouvableException("Annonce introuvable"));
    }

    /** Impossible d'agir sur l'annonce d'un autre en changeant l'id dans l'URL. */
    public Annonce chargerPourProprietaire(Long annonceId, Long utilisateurId) {
        Annonce annonce = charger(annonceId);
        if (!annonce.appartientA(utilisateurId)) {
            throw new AccesInterditException("Cette annonce ne t'appartient pas");
        }
        return annonce;
    }
}
