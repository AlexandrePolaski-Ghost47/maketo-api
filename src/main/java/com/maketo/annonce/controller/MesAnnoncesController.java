package com.maketo.annonce.controller;

import com.maketo.annonce.dto.MonAnnonceResponse;
import com.maketo.annonce.service.AnnonceService;
import com.maketo.common.dto.PageResponse;
import com.maketo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Espace "Mes annonces" (sous /utilisateurs/moi : toujours authentifié). */
@RestController
@RequestMapping("/api/utilisateurs/moi/annonces")
@RequiredArgsConstructor
public class MesAnnoncesController {

    private final AnnonceService annonceService;

    @GetMapping
    public PageResponse<MonAnnonceResponse> mesAnnonces(@AuthenticationPrincipal Jwt jwt,
                                                        @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.de(annonceService.mesAnnonces(SecurityUtils.idUtilisateur(jwt), pageable));
    }
}
