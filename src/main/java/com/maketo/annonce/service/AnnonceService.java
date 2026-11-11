package com.maketo.annonce.service;

import com.maketo.annonce.dto.AnnonceDetailResponse;
import com.maketo.annonce.dto.AnnonceRequest;
import com.maketo.annonce.dto.AnnonceResumeResponse;
import com.maketo.annonce.dto.ContactResponse;
import com.maketo.annonce.dto.CriteresRecherche;
import com.maketo.annonce.dto.MonAnnonceResponse;
import com.maketo.annonce.dto.VitrineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnnonceService {

    Page<AnnonceResumeResponse> rechercher(CriteresRecherche criteres, Pageable pageable);

    List<AnnonceResumeResponse> aLaUne(int limite);

    /** @param lecteurId null si visiteur non connecté */
    AnnonceDetailResponse consulter(Long annonceId, Long lecteurId, boolean lecteurAdmin);

    ContactResponse contacter(Long annonceId, Long utilisateurId);

    AnnonceDetailResponse publier(Long vendeurId, AnnonceRequest requete);

    AnnonceDetailResponse modifier(Long annonceId, Long utilisateurId, AnnonceRequest requete);

    AnnonceDetailResponse marquerVendue(Long annonceId, Long utilisateurId);

    AnnonceDetailResponse renouveler(Long annonceId, Long utilisateurId);

    void supprimer(Long annonceId, Long utilisateurId, boolean admin);

    Page<MonAnnonceResponse> mesAnnonces(Long vendeurId, Pageable pageable);

    VitrineResponse vitrine(Long vendeurId, boolean lecteurConnecte);

    Page<AnnonceResumeResponse> annoncesDuVendeur(Long vendeurId, Pageable pageable);
}
