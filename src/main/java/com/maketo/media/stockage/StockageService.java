package com.maketo.media.stockage;

/**
 * Abstraction du stockage des images. Le reste de l'appli ne sait pas OÙ sont les fichiers :
 * changer de fournisseur = nouvelle implémentation + une ligne de configuration (maketo.stockage.type).
 */
public interface StockageService {

    /** Stocke une image JPEG déjà préparée. */
    FichierStocke stocker(byte[] contenuJpeg);

    void supprimer(String identifiant);
}
