package com.maketo.categorie.service;

import com.maketo.categorie.dto.CategorieAdminResponse;
import com.maketo.categorie.dto.CategorieRequest;
import com.maketo.categorie.dto.CategorieResponse;
import com.maketo.categorie.entity.Categorie;
import com.maketo.categorie.mapper.CategorieMapper;
import com.maketo.categorie.repository.CategorieRepository;
import com.maketo.common.exception.ConflitException;
import com.maketo.common.exception.RessourceIntrouvableException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;
    private final CategorieMapper categorieMapper;

    @Override
    public List<CategorieResponse> listerActives() {
        return categorieMapper.versResponses(categorieRepository.findByActifTrueOrderByOrdreAffichageAsc());
    }

    @Override
    public Categorie chargerActive(Long id) {
        return categorieRepository.findById(id)
                .filter(Categorie::getActif)
                .orElseThrow(() -> new RessourceIntrouvableException("Catégorie introuvable"));
    }

    @Override
    public List<CategorieAdminResponse> listerToutes() {
        return categorieRepository.findAll(Sort.by("ordreAffichage")).stream().map(categorieMapper::versAdmin).toList();
    }

    @Override
    @Transactional
    public CategorieAdminResponse creer(CategorieRequest r) {
        if (categorieRepository.findBySlug(r.slug()).isPresent()) {
            throw new ConflitException("Ce slug est déjà utilisé");
        }
        Categorie categorie = Categorie.creer(r.nom().trim(), r.slug(), r.icone(), r.ordreAffichage());
        return categorieMapper.versAdmin(categorieRepository.saveAndFlush(categorie));
    }

    @Override
    @Transactional
    public CategorieAdminResponse modifier(Long id, CategorieRequest r) {
        Categorie categorie = charger(id);
        categorieRepository.findBySlug(r.slug())
                .filter(autre -> !autre.getId().equals(id))
                .ifPresent(autre -> {
                    throw new ConflitException("Ce slug est déjà utilisé");
                });
        categorie.setNom(r.nom().trim());
        categorie.setSlug(r.slug());
        categorie.setIcone(r.icone());
        categorie.setOrdreAffichage(r.ordreAffichage());
        categorieRepository.flush();
        return categorieMapper.versAdmin(categorie);
    }

    @Override
    @Transactional
    public CategorieAdminResponse changerActivation(Long id, boolean actif) {
        Categorie categorie = charger(id);
        categorie.setActif(actif);
        return categorieMapper.versAdmin(categorie);
    }

    private Categorie charger(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Catégorie introuvable"));
    }
}
