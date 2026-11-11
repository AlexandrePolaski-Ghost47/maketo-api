package com.maketo.credit.fournisseur;

/** @param cle identifiant à envoyer au fournisseur (ex. "MOMO", "OM") */
public record OperateurPaiement(String cle, String nom) {
}
