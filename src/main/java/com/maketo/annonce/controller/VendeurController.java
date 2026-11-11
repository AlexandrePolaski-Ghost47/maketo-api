package com.maketo.annonce.controller;

import com.maketo.annonce.dto.AnnonceResumeResponse;
import com.maketo.annonce.dto.VitrineResponse;
import com.maketo.annonce.service.AnnonceService;
import com.maketo.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Vitrine publique d'un vendeur (numéro visible uniquement si connecté). */
@RestController
@RequestMapping("/api/vendeurs/{vendeurId}")
@RequiredArgsConstructor
public class VendeurController {

    private final AnnonceService annonceService;

    @GetMapping
    public VitrineResponse vitrine(@PathVariable Long vendeurId, @AuthenticationPrincipal Jwt jwt) {
        return annonceService.vitrine(vendeurId, jwt != null);
    }

    @GetMapping("/annonces")
    public PageResponse<AnnonceResumeResponse> annonces(@PathVariable Long vendeurId,
                                                        @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.de(annonceService.annoncesDuVendeur(vendeurId, pageable));
    }
}
