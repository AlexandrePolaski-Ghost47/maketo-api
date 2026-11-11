package com.maketo.categorie.repository;

import com.maketo.categorie.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    List<Categorie> findByActifTrueOrderByOrdreAffichageAsc();

    Optional<Categorie> findBySlug(String slug);
}
