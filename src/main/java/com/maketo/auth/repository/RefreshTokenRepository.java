package com.maketo.auth.repository;

import com.maketo.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = "utilisateur")
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Déconnexion de tous les appareils (ou réaction à un vol de jeton). */
    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoque = true WHERE t.utilisateur.id = :utilisateurId AND t.revoque = false")
    int revoquerTousPourUtilisateur(@Param("utilisateurId") Long utilisateurId);

    /** Ménage périodique des jetons expirés ou révoqués. */
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expireLe < :maintenant OR t.revoque = true")
    int supprimerObsoletes(@Param("maintenant") Instant maintenant);
}
