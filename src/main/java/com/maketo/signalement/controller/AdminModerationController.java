package com.maketo.signalement.controller;

import com.maketo.common.dto.PageResponse;
import com.maketo.security.SecurityUtils;
import com.maketo.signalement.dto.SignalementResponse;
import com.maketo.signalement.entity.StatutSignalement;
import com.maketo.signalement.service.SignalementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/moderation")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminModerationController {

    private final SignalementService signalementService;

    @GetMapping("/signalements")
    public PageResponse<SignalementResponse> signalements(
            @RequestParam(defaultValue = "OUVERT") StatutSignalement statut,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.de(signalementService.lister(statut, pageable));
    }

    @PostMapping("/annonces/{annonceId}/retablir")
    public ResponseEntity<Void> retablir(@PathVariable Long annonceId, @AuthenticationPrincipal Jwt jwt) {
        signalementService.retablirAnnonce(annonceId, SecurityUtils.idUtilisateur(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/annonces/{annonceId}/retirer")
    public ResponseEntity<Void> retirer(@PathVariable Long annonceId, @AuthenticationPrincipal Jwt jwt) {
        signalementService.retirerAnnonce(annonceId, SecurityUtils.idUtilisateur(jwt));
        return ResponseEntity.noContent().build();
    }
}
