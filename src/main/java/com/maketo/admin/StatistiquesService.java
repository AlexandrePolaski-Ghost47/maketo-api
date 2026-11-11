package com.maketo.admin;

import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.annonce.repository.AnnonceRepository;
import com.maketo.common.util.Fuseau;
import com.maketo.credit.repository.PaiementRepository;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;
import com.maketo.signalement.entity.StatutSignalement;
import com.maketo.signalement.repository.SignalementRepository;
import com.maketo.utilisateur.entity.StatutUtilisateur;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiquesService {

    private final UtilisateurRepository utilisateurRepository;
    private final AnnonceRepository annonceRepository;
    private final SignalementRepository signalementRepository;
    private final PaiementRepository paiementRepository;
    private final ParametreService parametres;
    private final Clock clock;

    public StatistiquesResponse calculer() {
        Instant maintenant = clock.instant();
        Instant ilYA7Jours = maintenant.minus(Duration.ofDays(7));
        Instant debutDuMois = maintenant.atZone(Fuseau.CAMEROUN).toLocalDate().withDayOfMonth(1)
                .atStartOfDay(Fuseau.CAMEROUN).toInstant();

        return new StatistiquesResponse(
                utilisateurRepository.countByStatut(StatutUtilisateur.ACTIF),
                utilisateurRepository.countByStatut(StatutUtilisateur.EN_ATTENTE),
                utilisateurRepository.countByDateCreationAfter(ilYA7Jours),
                annonceRepository.countByStatutAndDateExpirationAfter(StatutAnnonce.ACTIVE, maintenant),
                annonceRepository.countByDateCreationAfter(ilYA7Jours),
                annonceRepository.countByStatut(StatutAnnonce.VENDUE),
                annonceRepository.countByStatut(StatutAnnonce.SUSPENDUE),
                signalementRepository.countByStatut(StatutSignalement.OUVERT),
                paiementRepository.sommeEncaisseeDepuis(debutDuMois).longValue(),
                paiementRepository.sommeFraisDepuis(debutDuMois).longValue(),
                parametres.booleen(ParametreCle.PAIEMENTS_ACTIFS));
    }
}
