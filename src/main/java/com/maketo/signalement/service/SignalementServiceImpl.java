package com.maketo.signalement.service;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.service.AnnonceChargeur;
import com.maketo.common.exception.ConflitException;
import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;
import com.maketo.signalement.dto.SignalementRequest;
import com.maketo.signalement.dto.SignalementResponse;
import com.maketo.signalement.entity.Signalement;
import com.maketo.signalement.entity.StatutSignalement;
import com.maketo.signalement.repository.SignalementRepository;
import com.maketo.utilisateur.entity.Utilisateur;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignalementServiceImpl implements SignalementService {

    private final SignalementRepository signalementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AnnonceChargeur annonceChargeur;
    private final ParametreService parametres;
    private final Clock clock;

    @Override
    @Transactional
    public void signaler(Long annonceId, Long auteurId, SignalementRequest r) {
        Annonce annonce = annonceChargeur.charger(annonceId);
        if (!annonce.estVisible(clock.instant())) {
            throw new RessourceIntrouvableException("Annonce introuvable");
        }
        if (annonce.appartientA(auteurId)) {
            throw new MetierException("Tu ne peux pas signaler ta propre annonce");
        }
        if (signalementRepository.existsByAnnonceIdAndAuteurId(annonceId, auteurId)) {
            throw new ConflitException("Tu as déjà signalé cette annonce");
        }
        Utilisateur auteur = utilisateurRepository.getReferenceById(auteurId);
        String commentaire = (r.commentaire() == null || r.commentaire().isBlank()) ? null : r.commentaire().trim();
        signalementRepository.save(Signalement.signaler(annonce, auteur, r.motif(), commentaire));

        // Protection de la communauté : au-delà du seuil, l'annonce est masquée sans attendre l'admin
        long ouverts = signalementRepository.countByAnnonceIdAndStatut(annonceId, StatutSignalement.OUVERT);
        int seuil = parametres.entier(ParametreCle.SEUIL_SUSPENSION_SIGNALEMENTS);
        if (ouverts >= seuil) {
            annonce.suspendre();
            log.info("Annonce {} suspendue automatiquement ({} signalements)", annonceId, ouverts);
        }
    }

    @Override
    public Page<SignalementResponse> lister(StatutSignalement statut, Pageable pageable) {
        Pageable simple = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return signalementRepository.findByStatutOrderByDateCreationAsc(statut, simple)
                .map(s -> new SignalementResponse(
                        s.getId(), s.getAnnonce().getId(), s.getAnnonce().getTitre(), s.getAnnonce().getStatut(),
                        s.getMotif(), s.getCommentaire(),
                        s.getAuteur().getPrenom() + " " + s.getAuteur().getNom(),
                        s.getStatut(), s.getDateCreation()));
    }

    @Override
    @Transactional
    public void retablirAnnonce(Long annonceId, Long adminId) {
        Annonce annonce = annonceChargeur.charger(annonceId);
        annonce.retablir();
        cloturerSignalements(annonceId, adminId);
    }

    @Override
    @Transactional
    public void retirerAnnonce(Long annonceId, Long adminId) {
        Annonce annonce = annonceChargeur.charger(annonceId);
        annonce.supprimer();
        cloturerSignalements(annonceId, adminId);
    }

    private void cloturerSignalements(Long annonceId, Long adminId) {
        signalementRepository.traiterTousPourAnnonce(annonceId,
                utilisateurRepository.getReferenceById(adminId), clock.instant());
    }
}
