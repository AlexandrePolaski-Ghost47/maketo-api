package com.maketo.credit.fournisseur;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Faux fournisseur pour le DÉVELOPPEMENT : aucun argent réel.
 * - le paiement réussit 10 secondes après l'initiation (le temps de "taper son PIN") ;
 * - un numéro se terminant par 0000 simule un échec (solde insuffisant).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "maketo.paiement", name = "fournisseur", havingValue = "simulation",
        matchIfMissing = true)
public class SimulationFournisseur implements FournisseurPaiement {

    private static final Duration DELAI_VALIDATION = Duration.ofSeconds(10);
    private static final int TAUX_FRAIS = 3;

    private final Clock clock;
    private final Map<String, Simulation> paiements = new ConcurrentHashMap<>();

    public SimulationFournisseur(Clock clock) {
        this.clock = clock;
        log.warn("=== Paiements en mode SIMULATION : aucun argent réel n'est encaissé ===");
    }

    @Override
    public String code() {
        return "SIMULATION";
    }

    @Override
    public List<OperateurPaiement> operateurs() {
        return List.of(new OperateurPaiement("MOMO", "MTN Mobile Money"),
                new OperateurPaiement("OM", "Orange Money"));
    }

    @Override
    public ResultatPaiement initier(DemandePaiement d) {
        String processCode = "SIM-" + d.reference();
        paiements.put(processCode, new Simulation(clock.instant(), d.montant(), d.telephone().endsWith("0000")));
        return new ResultatPaiement(processCode, EtatPaiementFournisseur.EN_ATTENTE, null,
                "Simulation : valide le paiement sur ton téléphone");
    }

    @Override
    public ResultatPaiement statut(String processCode) {
        Simulation s = paiements.get(processCode);
        if (s == null) {
            return ResultatPaiement.echec("Paiement inconnu (le serveur a redémarré)");
        }
        if (clock.instant().isBefore(s.debut().plus(DELAI_VALIDATION))) {
            return ResultatPaiement.inconnu(processCode, "En attente de validation");
        }
        if (s.echec()) {
            return new ResultatPaiement(processCode, EtatPaiementFournisseur.ECHOUE, null, "Solde insuffisant (simulation)");
        }
        return new ResultatPaiement(processCode, EtatPaiementFournisseur.REUSSI, s.montant() * TAUX_FRAIS / 100,
                "Paiement réussi (simulation)");
    }

    private record Simulation(Instant debut, int montant, boolean echec) {
    }
}
