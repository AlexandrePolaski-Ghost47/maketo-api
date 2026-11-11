package com.maketo.credit.controller;

import com.maketo.common.dto.PageResponse;
import com.maketo.credit.dto.CreditManuelRequest;
import com.maketo.credit.dto.PaiementAdminResponse;
import com.maketo.credit.dto.PortefeuilleResponse;
import com.maketo.credit.entity.StatutPaiement;
import com.maketo.credit.service.PaiementService;
import com.maketo.credit.service.PortefeuilleService;
import com.maketo.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCreditController {

    private final PaiementService paiementService;
    private final PortefeuilleService portefeuilleService;

    @GetMapping("/paiements")
    public PageResponse<PaiementAdminResponse> paiements(@RequestParam(required = false) StatutPaiement statut,
                                                         @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.de(paiementService.lister(statut, pageable));
    }

    @PostMapping("/utilisateurs/{utilisateurId}/credits")
    public PortefeuilleResponse crediter(@PathVariable Long utilisateurId,
                                         @Valid @RequestBody CreditManuelRequest requete,
                                         @AuthenticationPrincipal Jwt jwt) {
        return portefeuilleService.crediterManuellement(utilisateurId, requete, SecurityUtils.idUtilisateur(jwt));
    }
}
