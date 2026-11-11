package com.maketo.security;

import com.maketo.common.exception.TropDeTentativesException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti force brute : après 5 mots de passe faux pour un même numéro,
 * les connexions sont bloquées 15 minutes.
 * Stockage en mémoire (suffisant pour un seul serveur ; à déplacer vers Redis si on en ajoute d'autres).
 */
@Service
@RequiredArgsConstructor
public class ProtectionConnexionService {

    private static final int MAX_ECHECS = 5;
    private static final Duration DUREE_BLOCAGE = Duration.ofMinutes(15);

    private final Clock clock;
    private final Map<String, Tentatives> tentatives = new ConcurrentHashMap<>();

    public void verifierNonBloque(String cle) {
        Tentatives t = tentatives.get(cle);
        if (t == null) {
            return;
        }
        Instant maintenant = clock.instant();
        if (t.bloqueJusqua() != null && t.bloqueJusqua().isAfter(maintenant)) {
            long minutes = Math.max(1, Duration.between(maintenant, t.bloqueJusqua()).toMinutes());
            throw new TropDeTentativesException(minutes);
        }
        if (t.bloqueJusqua() != null) {
            tentatives.remove(cle); // blocage terminé : on repart de zéro
        }
    }

    public void enregistrerEchec(String cle) {
        Instant maintenant = clock.instant();
        tentatives.compute(cle, (k, t) -> {
            int echecs = (t == null ? 0 : t.echecs()) + 1;
            Instant bloqueJusqua = echecs >= MAX_ECHECS ? maintenant.plus(DUREE_BLOCAGE) : null;
            return new Tentatives(echecs, bloqueJusqua);
        });
    }

    public void reinitialiser(String cle) {
        tentatives.remove(cle);
    }

    private record Tentatives(int echecs, Instant bloqueJusqua) {
    }
}
