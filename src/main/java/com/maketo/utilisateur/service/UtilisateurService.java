package com.maketo.utilisateur.service;

import com.maketo.utilisateur.dto.MotDePasseRequest;
import com.maketo.utilisateur.dto.ProfilRequest;
import com.maketo.utilisateur.dto.UtilisateurResponse;
import com.maketo.utilisateur.entity.StatutUtilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UtilisateurService {

    UtilisateurResponse profil(Long utilisateurId);

    Page<UtilisateurResponse> listerParStatut(StatutUtilisateur statut, Pageable pageable);

    UtilisateurResponse activer(Long utilisateurId);

    UtilisateurResponse bloquer(Long utilisateurId, Long adminId);

    UtilisateurResponse modifierProfil(Long utilisateurId, ProfilRequest requete);

    void changerMotDePasse(Long utilisateurId, MotDePasseRequest requete);
}
