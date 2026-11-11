package com.maketo.credit.service;

import com.maketo.common.exception.MetierException;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;

/** Interrupteur global des paiements (paramètre paiements_actifs). */
final class PaiementsOuverts {

    private PaiementsOuverts() {
    }

    static void verifier(ParametreService parametres) {
        if (!parametres.booleen(ParametreCle.PAIEMENTS_ACTIFS)) {
            throw new MetierException("Les paiements ne sont pas encore ouverts : profite de la période de lancement gratuite !");
        }
    }
}
