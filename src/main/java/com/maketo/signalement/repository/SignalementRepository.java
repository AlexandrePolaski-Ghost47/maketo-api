package com.maketo.signalement.repository;

import com.maketo.signalement.entity.Signalement;
import com.maketo.signalement.entity.StatutSignalement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.maketo.utilisateur.entity.Utilisateur;

import java.time.Instant;

public interface SignalementRepository extends JpaRepository<Signalement, Long> {

    boolean existsByAnnonceIdAndAuteurId(Long annonceId, Long auteurId);

    @EntityGraph(attributePaths = {"annonce", "auteur"})
    Page<Signalement> findByStatutOrderByDateCreationAsc(StatutSignalement statut, Pageable pageable);

    long countByAnnonceIdAndStatut(Long annonceId, StatutSignalement statut);

    /** Clôture en une fois tous les signalements ouverts d'une annonce (décision de l'admin). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Signalement s
            SET s.statut = com.maketo.signalement.entity.StatutSignalement.TRAITE,
                s.traitePar = :admin,
                s.dateTraitement = :maintenant
            WHERE s.annonce.id = :annonceId
              AND s.statut = com.maketo.signalement.entity.StatutSignalement.OUVERT
            """)
    int traiterTousPourAnnonce(@Param("annonceId") Long annonceId, @Param("admin") Utilisateur admin,
                               @Param("maintenant") Instant maintenant);

    long countByStatut(StatutSignalement statut);
}
