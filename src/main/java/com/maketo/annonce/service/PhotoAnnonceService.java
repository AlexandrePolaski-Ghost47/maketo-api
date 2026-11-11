package com.maketo.annonce.service;

import com.maketo.annonce.dto.AnnonceDetailResponse;

public interface PhotoAnnonceService {

    AnnonceDetailResponse ajouter(Long annonceId, Long utilisateurId, byte[] contenu);

    AnnonceDetailResponse supprimer(Long annonceId, Long photoId, Long utilisateurId);
}
