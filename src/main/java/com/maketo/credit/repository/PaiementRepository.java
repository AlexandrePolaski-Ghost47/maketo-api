package com.maketo.credit.repository;

import com.maketo.credit.entity.Paiement;
import com.maketo.credit.entity.StatutPaiement;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    Optional<Paiement> findByReference(String reference);

    Optional<Paiement> findByProcessCode(String processCode);

    /**
     * Verrou exclusif (SELECT ... FOR UPDATE) : si le webhook et la vérification de statut arrivent en même temps,
     * l'un attend l'autre, et le portefeuille n'est crédité qu'une fois.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Paiement p WHERE p.id = :id")
    Optional<Paiement> verrouiller(@Param("id") Long id);

    List<Paiement> findByStatutAndDateCreationBeforeOrderByDateCreationAsc(StatutPaiement statut, Instant avant,
                                                                          Pageable pageable);

    long countByUtilisateurIdAndStatutAndDateCreationAfter(Long utilisateurId, StatutPaiement statut, Instant depuis);

    @EntityGraph(attributePaths = "utilisateur")
    Page<Paiement> findByStatutOrderByDateCreationDesc(StatutPaiement statut, Pageable pageable);

    @EntityGraph(attributePaths = "utilisateur")
    Page<Paiement> findAllByOrderByDateCreationDesc(Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p
            WHERE p.statut = com.maketo.credit.entity.StatutPaiement.REUSSI AND p.dateFinalisation >= :depuis
            """)
    Number sommeEncaisseeDepuis(@Param("depuis") Instant depuis);

    @Query("""
            SELECT COALESCE(SUM(p.frais), 0) FROM Paiement p
            WHERE p.statut = com.maketo.credit.entity.StatutPaiement.REUSSI AND p.dateFinalisation >= :depuis
            """)
    Number sommeFraisDepuis(@Param("depuis") Instant depuis);
}
