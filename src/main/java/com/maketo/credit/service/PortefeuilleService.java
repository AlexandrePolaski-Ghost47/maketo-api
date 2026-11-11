package com.maketo.credit.service;

import com.maketo.credit.dto.AchatResponse;
import com.maketo.credit.dto.CreditManuelRequest;
import com.maketo.credit.dto.OffreResponse;
import com.maketo.credit.dto.PortefeuilleResponse;
import com.maketo.credit.dto.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PortefeuilleService {

    List<OffreResponse> offres();

    PortefeuilleResponse portefeuille(Long utilisateurId);

    Page<TransactionResponse> historique(Long utilisateurId, Pageable pageable);

    AchatResponse booster(Long annonceId, Long utilisateurId, String offreCode);

    AchatResponse passerPro(Long utilisateurId, String offreCode);

    PortefeuilleResponse crediterManuellement(Long utilisateurId, CreditManuelRequest requete, Long adminId);
}
