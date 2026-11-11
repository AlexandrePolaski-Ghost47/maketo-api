package com.maketo.utilisateur.dto;

import com.maketo.utilisateur.entity.Role;
import com.maketo.utilisateur.entity.StatutUtilisateur;

import java.time.Instant;

/** Profil renvoyé par l'API (jamais le mot de passe, jamais l'entité). */
public record UtilisateurResponse(
        Long id,
        String nom,
        String prenom,
        String telephone,
        String whatsapp,
        String matricule,
        String filiere,
        String niveau,
        Role role,
        StatutUtilisateur statut,
        boolean pro,
        Instant proExpireLe,
        Integer soldeCredits,
        Instant dateInscription
) {
}
