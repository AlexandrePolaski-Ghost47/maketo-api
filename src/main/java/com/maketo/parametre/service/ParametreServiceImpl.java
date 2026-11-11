package com.maketo.parametre.service;

import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.parametre.dto.ParametreResponse;
import com.maketo.parametre.entity.Parametre;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.entity.TypeParametre;
import com.maketo.parametre.repository.ParametreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Les paramètres sont lus très souvent et changent rarement : on les garde en mémoire
 * 5 minutes (ou jusqu'à une modification depuis l'admin).
 */
@Service
@RequiredArgsConstructor
public class ParametreServiceImpl implements ParametreService {

    private static final Duration DUREE_CACHE = Duration.ofMinutes(5);

    private final ParametreRepository parametreRepository;
    private final Clock clock;

    private volatile Map<String, String> cache;
    private volatile Instant chargeLe;

    @Override
    public String texte(ParametreCle cle) {
        String valeur = cache().get(cle.getCle());
        if (valeur == null) {
            throw new IllegalStateException("Paramètre manquant en base : " + cle.getCle());
        }
        return valeur.trim();
    }

    @Override
    public int entier(ParametreCle cle) {
        return Integer.parseInt(texte(cle));
    }

    @Override
    public LocalDate date(ParametreCle cle) {
        return LocalDate.parse(texte(cle));
    }

    @Override
    public boolean booleen(ParametreCle cle) {
        return Boolean.parseBoolean(texte(cle));
    }

    @Override
    public List<Integer> listeEntiers(ParametreCle cle) {
        return Arrays.stream(texte(cle).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParametreResponse> lister() {
        return parametreRepository.findAll(Sort.by("cle")).stream().map(this::versResponse).toList();
    }

    @Override
    @Transactional
    public ParametreResponse modifier(String cle, String valeur) {
        ParametreCle connue = ParametreCle.depuis(cle)
                .orElseThrow(() -> new RessourceIntrouvableException("Paramètre inconnu : " + cle));
        String propre = valeur.trim();
        valider(connue.getType(), propre);

        Parametre parametre = parametreRepository.findById(cle)
                .orElseThrow(() -> new RessourceIntrouvableException("Paramètre introuvable : " + cle));
        parametre.modifierValeur(propre);
        parametreRepository.flush();
        cache = null; // invalidation : relu au prochain accès
        return versResponse(parametre);
    }

    private static void valider(TypeParametre type, String valeur) {
        try {
            switch (type) {
                case ENTIER -> {
                    if (Integer.parseInt(valeur) < 0) throw new MetierException("La valeur doit être positive");
                }
                case DATE -> LocalDate.parse(valeur);
                case BOOLEEN -> {
                    if (!valeur.equals("true") && !valeur.equals("false")) {
                        throw new MetierException("Valeur attendue : true ou false");
                    }
                }
                case LISTE_ENTIERS -> {
                    List<String> morceaux = Arrays.stream(valeur.split(",")).map(String::trim).toList();
                    if (morceaux.isEmpty()) throw new MetierException("Liste vide");
                    for (String m : morceaux) {
                        if (Integer.parseInt(m) <= 0) throw new MetierException("Chaque montant doit être positif");
                    }
                }
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new MetierException("Format invalide pour ce paramètre (" + type + ")");
        }
    }

    private ParametreResponse versResponse(Parametre p) {
        TypeParametre type = ParametreCle.depuis(p.getCle()).map(ParametreCle::getType).orElse(null);
        return new ParametreResponse(p.getCle(), p.getValeur(), type, p.getDescription(), p.getDateModification());
    }

    private Map<String, String> cache() {
        Map<String, String> actuel = cache;
        if (actuel != null && chargeLe.plus(DUREE_CACHE).isAfter(clock.instant())) {
            return actuel;
        }
        synchronized (this) {
            if (cache == null || chargeLe.plus(DUREE_CACHE).isBefore(clock.instant())) {
                cache = parametreRepository.findAll().stream()
                        .collect(Collectors.toUnmodifiableMap(Parametre::getCle, Parametre::getValeur));
                chargeLe = clock.instant();
            }
            return cache;
        }
    }
}
