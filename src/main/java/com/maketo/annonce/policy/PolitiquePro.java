package com.maketo.annonce.policy;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.Formule;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;
import com.maketo.utilisateur.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** Vendeur Pro : publications illimitées, visibles 30 jours. */
@Component
@Order(1)
@RequiredArgsConstructor
public class PolitiquePro implements PolitiquePublication {

    private final ParametreService parametres;

    @Override
    public Formule formule() {
        return Formule.PRO;
    }

    @Override
    public boolean sApplique(Utilisateur vendeur, Instant maintenant) {
        return vendeur.estPro(maintenant);
    }

    @Override
    public void verifierDroit(Utilisateur vendeur, Annonce annonceRenouvelee, Instant maintenant) {
        // aucune limite
    }

    @Override
    public Instant dateExpiration(Instant maintenant) {
        return maintenant.plus(Duration.ofDays(parametres.entier(ParametreCle.DUREE_ANNONCE_PRO_JOURS)));
    }
}
