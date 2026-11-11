package com.maketo.signalement.controller;

import com.maketo.security.SecurityUtils;
import com.maketo.signalement.dto.SignalementRequest;
import com.maketo.signalement.service.SignalementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignalementController {

    private final SignalementService signalementService;

    @PostMapping("/api/annonces/{annonceId}/signalements")
    public ResponseEntity<Void> signaler(@PathVariable Long annonceId,
                                         @Valid @RequestBody SignalementRequest requete,
                                         @AuthenticationPrincipal Jwt jwt) {
        signalementService.signaler(annonceId, SecurityUtils.idUtilisateur(jwt), requete);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
