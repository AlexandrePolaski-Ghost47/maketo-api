package com.maketo.favori.controller;

import com.maketo.common.dto.PageResponse;
import com.maketo.favori.dto.FavoriResponse;
import com.maketo.favori.service.FavoriService;
import com.maketo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FavoriController {

    private final FavoriService favoriService;

    @PostMapping("/api/annonces/{annonceId}/favori")
    public ResponseEntity<Void> ajouter(@PathVariable Long annonceId, @AuthenticationPrincipal Jwt jwt) {
        favoriService.ajouter(annonceId, SecurityUtils.idUtilisateur(jwt));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/annonces/{annonceId}/favori")
    public ResponseEntity<Void> retirer(@PathVariable Long annonceId, @AuthenticationPrincipal Jwt jwt) {
        favoriService.retirer(annonceId, SecurityUtils.idUtilisateur(jwt));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/utilisateurs/moi/favoris")
    public PageResponse<FavoriResponse> lister(@AuthenticationPrincipal Jwt jwt,
                                               @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.de(favoriService.lister(SecurityUtils.idUtilisateur(jwt), pageable));
    }

    @GetMapping("/api/utilisateurs/moi/favoris/ids")
    public List<Long> ids(@AuthenticationPrincipal Jwt jwt) {
        return favoriService.idsAnnonces(SecurityUtils.idUtilisateur(jwt));
    }
}
