package com.maketo.credit.dto;

import com.maketo.credit.fournisseur.OperateurPaiement;

import java.util.List;

/** Tout ce dont l'écran "Recharger" a besoin en un seul appel. */
public record OptionsRechargeResponse(boolean paiementsActifs, List<Integer> montants,
                                      List<OperateurPaiement> operateurs) {
}
