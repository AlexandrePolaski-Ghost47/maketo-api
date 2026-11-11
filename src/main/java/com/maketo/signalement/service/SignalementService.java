package com.maketo.signalement.service;

import com.maketo.signalement.dto.SignalementRequest;
import com.maketo.signalement.dto.SignalementResponse;
import com.maketo.signalement.entity.StatutSignalement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SignalementService {

    void signaler(Long annonceId, Long auteurId, SignalementRequest requete);

    Page<SignalementResponse> lister(StatutSignalement statut, Pageable pageable);

    /** L'annonce était correcte : elle redevient visible et ses signalements sont clôturés. */
    void retablirAnnonce(Long annonceId, Long adminId);

    /** L'annonce enfreint les règles : elle est retirée et ses signalements sont clôturés. */
    void retirerAnnonce(Long annonceId, Long adminId);
}
