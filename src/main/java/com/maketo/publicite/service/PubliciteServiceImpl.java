package com.maketo.publicite.service;

import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.common.util.ApresTransaction;
import com.maketo.common.util.Fuseau;
import com.maketo.media.image.TraitementImage;
import com.maketo.media.stockage.FichierStocke;
import com.maketo.media.stockage.StockageService;
import com.maketo.publicite.dto.PubliciteAdminResponse;
import com.maketo.publicite.dto.PubliciteRequest;
import com.maketo.publicite.dto.PubliciteResponse;
import com.maketo.publicite.entity.Publicite;
import com.maketo.publicite.repository.PubliciteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PubliciteServiceImpl implements PubliciteService {

    private final PubliciteRepository publiciteRepository;
    private final TraitementImage traitementImage;
    private final StockageService stockage;
    private final Clock clock;

    @Override
    public List<PubliciteResponse> actives() {
        return publiciteRepository.actives(aujourdhui()).stream()
                .map(p -> new PubliciteResponse(p.getId(), p.getAnnonceur(), p.getTitre(), p.getImageUrl(), p.getLien()))
                .toList();
    }

    @Override
    public void enregistrerClic(Long id) {
        publiciteRepository.incrementerClics(id);
    }

    @Override
    @Transactional
    public PubliciteAdminResponse creer(PubliciteRequest r, byte[] image) {
        FichierStocke fichier = stockage.stocker(traitementImage.preparer(image));
        ApresTransaction.siAnnulee(() -> stockage.supprimer(fichier.identifiant()));

        Publicite publicite = Publicite.creer(r.annonceur().trim(), vide(r.titre()) ? null : r.titre().trim(),
                fichier.url(), fichier.identifiant(), vide(r.lien()) ? null : r.lien().trim(),
                r.dateDebut(), r.dateFin());
        return versAdmin(publiciteRepository.saveAndFlush(publicite));
    }

    @Override
    public Page<PubliciteAdminResponse> lister(Pageable pageable) {
        Pageable simple = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return publiciteRepository.findAllByOrderByDateCreationDesc(simple).map(this::versAdmin);
    }

    @Override
    @Transactional
    public PubliciteAdminResponse changerActivation(Long id, boolean actif) {
        Publicite publicite = publiciteRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Publicité introuvable"));
        if (actif) publicite.activer(); else publicite.desactiver();
        return versAdmin(publicite);
    }

    private PubliciteAdminResponse versAdmin(Publicite p) {
        LocalDate jour = aujourdhui();
        boolean enCours = p.getActif() && !jour.isBefore(p.getDateDebut()) && !jour.isAfter(p.getDateFin());
        return new PubliciteAdminResponse(p.getId(), p.getAnnonceur(), p.getTitre(), p.getImageUrl(), p.getLien(),
                p.getDateDebut(), p.getDateFin(), p.getActif(), enCours, p.getNbClics());
    }

    private LocalDate aujourdhui() {
        return LocalDate.now(clock.withZone(Fuseau.CAMEROUN));
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
