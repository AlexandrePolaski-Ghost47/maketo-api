package com.maketo.annonce.dto;

import com.maketo.annonce.entity.EtatArticle;
import com.maketo.annonce.entity.TypeAnnonce;

public record CriteresRecherche(String categorie, String q, Integer prixMin, Integer prixMax, EtatArticle etat,
                                TypeAnnonce type) {
}
