package com.maketo.annonce.policy;

import com.maketo.utilisateur.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/** Choisit la première politique applicable (Spring injecte la liste triée selon @Order). */
@Component
@RequiredArgsConstructor
public class SelecteurPolitique {

    private final List<PolitiquePublication> politiques;

    public PolitiquePublication pour(Utilisateur vendeur, Instant maintenant) {
        return politiques.stream()
                .filter(p -> p.sApplique(vendeur, maintenant))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucune politique de publication applicable"));
    }
}
