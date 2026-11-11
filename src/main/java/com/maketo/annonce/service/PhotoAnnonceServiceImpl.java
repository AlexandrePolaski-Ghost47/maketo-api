package com.maketo.annonce.service;

import com.maketo.annonce.dto.AnnonceDetailResponse;
import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.PhotoAnnonce;
import com.maketo.annonce.mapper.AnnonceMapper;
import com.maketo.annonce.repository.AnnonceRepository;
import com.maketo.common.exception.MetierException;
import com.maketo.common.util.ApresTransaction;
import com.maketo.media.image.TraitementImage;
import com.maketo.media.stockage.FichierStocke;
import com.maketo.media.stockage.StockageService;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class PhotoAnnonceServiceImpl implements PhotoAnnonceService {

    private final AnnonceChargeur chargeur;
    private final AnnonceRepository annonceRepository;
    private final TraitementImage traitementImage;
    private final StockageService stockage;
    private final ParametreService parametres;
    private final AnnonceMapper annonceMapper;
    private final Clock clock;

    @Override
    @Transactional
    public AnnonceDetailResponse ajouter(Long annonceId, Long utilisateurId, byte[] contenu) {
        Annonce annonce = chargeur.chargerPourProprietaire(annonceId, utilisateurId);
        int max = parametres.entier(ParametreCle.NB_MAX_PHOTOS);
        if (annonce.getPhotos().size() >= max) {
            // vérifié AVANT le traitement : on ne gaspille pas de calcul pour une photo refusée
            throw new MetierException("Maximum " + max + " photos par annonce");
        }

        byte[] jpeg = traitementImage.preparer(contenu);
        FichierStocke fichier = stockage.stocker(jpeg);
        ApresTransaction.siAnnulee(() -> stockage.supprimer(fichier.identifiant()));

        annonce.ajouterPhoto(fichier.url(), fichier.identifiant(), max);
        annonceRepository.flush(); // attribue l'id de la nouvelle photo avant de répondre
        return annonceMapper.versDetail(annonce, true, clock.instant());
    }

    @Override
    @Transactional
    public AnnonceDetailResponse supprimer(Long annonceId, Long photoId, Long utilisateurId) {
        Annonce annonce = chargeur.chargerPourProprietaire(annonceId, utilisateurId);
        PhotoAnnonce photo = annonce.retirerPhoto(photoId);
        ApresTransaction.siValidee(() -> stockage.supprimer(photo.getPublicId()));
        return annonceMapper.versDetail(annonce, true, clock.instant());
    }
}
