package com.maketo.media.stockage;

/**
 * @param url         adresse publique de l'image
 * @param identifiant identifiant chez le fournisseur (nécessaire pour supprimer)
 */
public record FichierStocke(String url, String identifiant) {
}
