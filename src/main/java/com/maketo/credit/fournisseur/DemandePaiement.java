package com.maketo.credit.fournisseur;

/**
 * @param reference   notre identifiant unique (jamais réutilisé)
 * @param telephone   numéro au format "6XXXXXXXX"
 * @param callbackUrl URL du webhook, ou null
 */
public record DemandePaiement(String reference, int montant, String motif, String operateur, String telephone,
                              String callbackUrl) {
}
