package com.maketo.favori.service;

import com.maketo.favori.dto.FavoriResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FavoriService {

    void ajouter(Long annonceId, Long utilisateurId);

    void retirer(Long annonceId, Long utilisateurId);

    Page<FavoriResponse> lister(Long utilisateurId, Pageable pageable);

    List<Long> idsAnnonces(Long utilisateurId);
}
