package com.maketo.credit.controller;

import com.maketo.common.dto.PageResponse;
import com.maketo.credit.dto.AchatRequest;
import com.maketo.credit.dto.AchatResponse;
import com.maketo.credit.dto.OffreResponse;
import com.maketo.credit.dto.PortefeuilleResponse;
import com.maketo.credit.dto.TransactionResponse;
import com.maketo.credit.service.PortefeuilleService;
import com.maketo.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PortefeuilleController {

    private final PortefeuilleService portefeuilleService;

    @GetMapping("/api/offres")
    public List<OffreResponse> offres() {
        return portefeuilleService.offres();
    }

    @GetMapping("/api/utilisateurs/moi/portefeuille")
    public PortefeuilleResponse portefeuille(@AuthenticationPrincipal Jwt jwt) {
        return portefeuilleService.portefeuille(SecurityUtils.idUtilisateur(jwt));
    }

    @GetMapping("/api/utilisateurs/moi/transactions")
    public PageResponse<TransactionResponse> transactions(@AuthenticationPrincipal Jwt jwt,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.de(portefeuilleService.historique(SecurityUtils.idUtilisateur(jwt), pageable));
    }

    @PostMapping("/api/annonces/{annonceId}/boost")
    public AchatResponse booster(@PathVariable Long annonceId,
                                 @Valid @RequestBody(required = false) AchatRequest requete,
                                 @AuthenticationPrincipal Jwt jwt) {
        return portefeuilleService.booster(annonceId, SecurityUtils.idUtilisateur(jwt),
                requete == null ? null : requete.offreCode());
    }

    @PostMapping("/api/abonnements/pro")
    public AchatResponse passerPro(@Valid @RequestBody(required = false) AchatRequest requete,
                                   @AuthenticationPrincipal Jwt jwt) {
        return portefeuilleService.passerPro(SecurityUtils.idUtilisateur(jwt),
                requete == null ? null : requete.offreCode());
    }
}
