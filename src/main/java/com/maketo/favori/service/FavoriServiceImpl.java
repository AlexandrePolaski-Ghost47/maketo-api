package com.maketo.favori.service;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.mapper.AnnonceMapper;
import com.maketo.annonce.service.AnnonceChargeur;
import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.favori.dto.FavoriResponse;
import com.maketo.favori.entity.Favori;
import com.maketo.favori.entity.FavoriId;
import com.maketo.favori.repository.FavoriRepository;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriServiceImpl implements FavoriService {

    private final FavoriRepository favoriRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AnnonceChargeur annonceChargeur;
    private final AnnonceMapper annonceMapper;
    private final Clock clock;

    /** Idempotent : ajouter deux fois ne crée pas de doublon et ne renvoie pas d'erreur. */
    @Override
    @Transactional
    public void ajouter(Long annonceId, Long utilisateurId) {
        if (favoriRepository.existsById(new FavoriId(utilisateurId, annonceId))) {
            return;
        }
        Annonce annonce = annonceChargeur.charger(annonceId);
        if (!annonce.estVisible(clock.instant())) {
            throw new RessourceIntrouvableException("Annonce introuvable");
        }
        if (annonce.appartientA(utilisateurId)) {
            throw new MetierException("Tu ne peux pas mettre ta propre annonce en favori");
        }
        favoriRepository.save(Favori.ajouter(utilisateurRepository.getReferenceById(utilisateurId), annonce));
    }

    /** Idempotent également. */
    @Override
    @Transactional
    public void retirer(Long annonceId, Long utilisateurId) {
        FavoriId id = new FavoriId(utilisateurId, annonceId);
        if (favoriRepository.existsById(id)) {
            favoriRepository.deleteById(id);
        }
    }

    @Override
    public Page<FavoriResponse> lister(Long utilisateurId, Pageable pageable) {
        Instant maintenant = clock.instant();
        Pageable simple = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return favoriRepository.findByUtilisateurIdOrderByDateAjoutDesc(utilisateurId, simple)
                .map(f -> new FavoriResponse(
                        annonceMapper.versResume(f.getAnnonce(), maintenant),
                        f.getAnnonce().estVisible(maintenant),
                        f.getDateAjout()));
    }

    @Override
    public List<Long> idsAnnonces(Long utilisateurId) {
        return favoriRepository.idsAnnoncesFavorites(utilisateurId);
    }
}
