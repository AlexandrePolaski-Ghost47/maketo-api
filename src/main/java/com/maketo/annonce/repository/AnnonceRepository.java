package com.maketo.annonce.repository;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.StatutAnnonce;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * JpaSpecificationExecutor : la recherche multi-critères (sprint 3) se fera par
 * Specifications, sans ajouter une méthode par combinaison de filtres.
 */
public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

    /** Fiche annonce complète en une seule requête (évite le problème N+1). */
    @EntityGraph(attributePaths = {"vendeur", "categorie", "photos"})
    Optional<Annonce> findDetailById(Long id);

    /** Annonces d'un vendeur (son espace « Mes annonces »), hors supprimées. */
    Page<Annonce> findByVendeurIdAndStatutNot(Long vendeurId, StatutAnnonce statut, Pageable pageable);

    /** Annonces « À la une » : actives, non expirées, boost en cours. */
    @Query("""
            SELECT a FROM Annonce a
            WHERE a.statut = com.maketo.annonce.entity.StatutAnnonce.ACTIVE
              AND a.dateExpiration > :maintenant
              AND a.boostJusquA > :maintenant
            ORDER BY a.boostJusquA DESC
            """)
    Page<Annonce> findALaUne(@Param("maintenant") Instant maintenant, Pageable pageable);

    /** Publications gratuites (créations ou renouvellements) d'un vendeur depuis une date : sert au quota mensuel. */
    @Query("""
            SELECT COUNT(a) FROM Annonce a
            WHERE a.vendeur.id = :vendeurId
              AND a.formule = com.maketo.annonce.entity.Formule.GRATUITE
              AND (a.dateCreation >= :depuis OR a.dateDernierRenouvellement >= :depuis)
            """)
    long compterPublicationsGratuitesDepuis(@Param("vendeurId") Long vendeurId, @Param("depuis") Instant depuis);

    /** Tâche planifiée : passe en EXPIREE toutes les annonces arrivées à échéance. */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Annonce a
            SET a.statut = com.maketo.annonce.entity.StatutAnnonce.EXPIREE,
                a.dateModification = :maintenant
            WHERE a.statut = com.maketo.annonce.entity.StatutAnnonce.ACTIVE
              AND a.dateExpiration <= :maintenant
            """)
    int expirerAnnoncesEchues(@Param("maintenant") Instant maintenant);

    /** Incréments atomiques : pas de perte si deux personnes consultent en même temps. */
    @Transactional
    @Modifying
    @Query("UPDATE Annonce a SET a.nbVues = a.nbVues + 1 WHERE a.id = :id")
    int incrementerVues(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE Annonce a SET a.nbContacts = a.nbContacts + 1 WHERE a.id = :id")
    int incrementerContacts(@Param("id") Long id);

    // --- Statistiques admin ---
    long countByStatut(StatutAnnonce statut);

    long countByStatutAndDateExpirationAfter(StatutAnnonce statut, Instant maintenant);

    long countByDateCreationAfter(Instant depuis);
}
