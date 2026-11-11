package com.maketo.favori.repository;

import com.maketo.favori.entity.Favori;
import com.maketo.favori.entity.FavoriId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriRepository extends JpaRepository<Favori, FavoriId> {

    @EntityGraph(attributePaths = "annonce")
    Page<Favori> findByUtilisateurIdOrderByDateAjoutDesc(Long utilisateurId, Pageable pageable);

    /** Ids des annonces en favori : permet au front d'afficher le cœur plein sans appel par annonce. */
    @Query("SELECT f.annonce.id FROM Favori f WHERE f.utilisateur.id = :utilisateurId")
    List<Long> idsAnnoncesFavorites(@Param("utilisateurId") Long utilisateurId);
}
