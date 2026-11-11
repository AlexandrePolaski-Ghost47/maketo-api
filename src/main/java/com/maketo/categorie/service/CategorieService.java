package com.maketo.categorie.service;

import com.maketo.categorie.dto.CategorieAdminResponse;
import com.maketo.categorie.dto.CategorieRequest;
import com.maketo.categorie.dto.CategorieResponse;
import com.maketo.categorie.entity.Categorie;

import java.util.List;

public interface CategorieService {

    List<CategorieResponse> listerActives();

    /** Charge une catégorie utilisable pour publier (existe et active). */
    Categorie chargerActive(Long id);

    // --- Admin ---
    List<CategorieAdminResponse> listerToutes();

    CategorieAdminResponse creer(CategorieRequest requete);

    CategorieAdminResponse modifier(Long id, CategorieRequest requete);

    /** Une catégorie désactivée disparaît des choix de publication ; ses annonces restent visibles. */
    CategorieAdminResponse changerActivation(Long id, boolean actif);
}
