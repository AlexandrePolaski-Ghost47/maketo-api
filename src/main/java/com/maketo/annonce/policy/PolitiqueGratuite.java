package com.maketo.annonce.policy;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.Formule;
import com.maketo.annonce.repository.AnnonceRepository;
import com.maketo.common.exception.MetierException;
import com.maketo.common.util.Fuseau;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;
import com.maketo.utilisateur.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * Formule gratuite : X annonces différentes par mois, visibles 7 jours.
 * Pas de quota pendant la période de lancement.
 */
@Component
@Order(100) // politique par défaut : évaluée en dernier
@RequiredArgsConstructor
public class PolitiqueGratuite implements PolitiquePublication {

    private final ParametreService parametres;
    private final AnnonceRepository annonceRepository;

    @Override
    public Formule formule() {
        return Formule.GRATUITE;
    }

    @Override
    public boolean sApplique(Utilisateur vendeur, Instant maintenant) {
        return true;
    }

    @Override
    public void verifierDroit(Utilisateur vendeur, Annonce annonceRenouvelee, Instant maintenant) {
        if (estEnPeriodeDeLancement(maintenant)) {
            return;
        }
        Instant debutDuMois = debutDuMois(maintenant);

        // Une annonce déjà publiée ou renouvelée ce mois-ci est déjà comptée : la renouveler ne coûte rien de plus
        if (annonceRenouvelee != null && dejaCompteeCeMois(annonceRenouvelee, debutDuMois)) {
            return;
        }

        int quota = parametres.entier(ParametreCle.QUOTA_GRATUIT_MENSUEL);
        long utilisees = annonceRepository.compterPublicationsGratuitesDepuis(vendeur.getId(), debutDuMois);
        if (utilisees >= quota) {
            throw new MetierException("Tu as utilisé tes " + quota + " publications gratuites ce mois-ci. "
                    + "Passe Vendeur Pro pour publier sans limite.");
        }
    }

    @Override
    public Instant dateExpiration(Instant maintenant) {
        return maintenant.plus(Duration.ofDays(parametres.entier(ParametreCle.DUREE_ANNONCE_GRATUITE_JOURS)));
    }

    private boolean estEnPeriodeDeLancement(Instant maintenant) {
        Instant fin = parametres.date(ParametreCle.DATE_FIN_PERIODE_GRATUITE)
                .plusDays(1).atStartOfDay(Fuseau.CAMEROUN).toInstant(); // date de fin incluse
        return maintenant.isBefore(fin);
    }

    private static Instant debutDuMois(Instant maintenant) {
        ZonedDateTime ici = maintenant.atZone(Fuseau.CAMEROUN);
        return ici.toLocalDate().withDayOfMonth(1).atStartOfDay(Fuseau.CAMEROUN).toInstant();
    }

    private static boolean dejaCompteeCeMois(Annonce annonce, Instant debutDuMois) {
        if (annonce.getFormule() != Formule.GRATUITE) {
            return false;
        }
        boolean creeCeMois = !annonce.getDateCreation().isBefore(debutDuMois);
        boolean renouveleeCeMois = annonce.getDateDernierRenouvellement() != null
                && !annonce.getDateDernierRenouvellement().isBefore(debutDuMois);
        return creeCeMois || renouveleeCeMois;
    }
}
