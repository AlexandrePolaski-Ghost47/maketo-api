package com.maketo.utilisateur.repository;

import com.maketo.utilisateur.entity.StatutUtilisateur;
import com.maketo.utilisateur.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByTelephone(String telephone);

    boolean existsByTelephone(String telephone);

    boolean existsByMatricule(String matricule);

    Page<Utilisateur> findByStatut(StatutUtilisateur statut, Pageable pageable);

    long countByStatut(StatutUtilisateur statut);

    long countByDateCreationAfter(Instant depuis);
}
