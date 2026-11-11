package com.maketo.utilisateur.controller;

import com.maketo.common.dto.PageResponse;
import com.maketo.security.SecurityUtils;
import com.maketo.utilisateur.dto.UtilisateurResponse;
import com.maketo.utilisateur.entity.StatutUtilisateur;
import com.maketo.utilisateur.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Gestion des comptes par l'administrateur (validation des inscriptions, blocage). */
@RestController
@RequestMapping("/api/admin/utilisateurs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public PageResponse<UtilisateurResponse> lister(
            @RequestParam(defaultValue = "EN_ATTENTE") StatutUtilisateur statut,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.de(utilisateurService.listerParStatut(statut, pageable));
    }

    @PatchMapping("/{id}/activer")
    public UtilisateurResponse activer(@PathVariable Long id) {
        return utilisateurService.activer(id);
    }

    @PatchMapping("/{id}/bloquer")
    public UtilisateurResponse bloquer(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return utilisateurService.bloquer(id, SecurityUtils.idUtilisateur(jwt));
    }
}
