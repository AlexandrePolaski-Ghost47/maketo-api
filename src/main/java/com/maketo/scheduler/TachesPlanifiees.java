package com.maketo.scheduler;

import com.maketo.annonce.repository.AnnonceRepository;
import com.maketo.auth.repository.RefreshTokenRepository;
import com.maketo.credit.service.PaiementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Tâches de fond. Fréquences réglables dans la configuration (maketo.taches.*).
 * Si un jour l'API tourne sur plusieurs serveurs, ajouter un verrou (ex. ShedLock)
 * pour qu'une tâche ne s'exécute qu'une fois.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TachesPlanifiees {

    private final AnnonceRepository annonceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PaiementService paiementService;
    private final Clock clock;

    /** Passe en EXPIREE les annonces arrivées à échéance (par défaut toutes les heures). */
    @Scheduled(cron = "${maketo.taches.expiration-cron:0 0 * * * *}", zone = "Africa/Douala")
    public void expirerAnnonces() {
        int nb = annonceRepository.expirerAnnoncesEchues(clock.instant());
        if (nb > 0) {
            log.info("{} annonce(s) expirée(s)", nb);
        }
    }

    /** Supprime les sessions expirées ou révoquées (par défaut chaque nuit à 3 h). */
    @Scheduled(cron = "${maketo.taches.nettoyage-cron:0 0 3 * * *}", zone = "Africa/Douala")
    public void nettoyerSessions() {
        int nb = refreshTokenRepository.supprimerObsoletes(clock.instant());
        log.info("{} session(s) obsolète(s) supprimée(s)", nb);
    }

    /** Revérifie les recharges restées en attente (webhook perdu, URL non publique en dev...). */
    @Scheduled(cron = "${maketo.taches.paiements-cron:0 */2 * * * *}", zone = "Africa/Douala")
    public void reconcilierPaiements() {
        paiementService.reconcilier();
    }
}
