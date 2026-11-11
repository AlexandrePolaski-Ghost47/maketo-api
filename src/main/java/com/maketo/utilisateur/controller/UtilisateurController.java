package com.maketo.utilisateur.controller;

import com.maketo.security.SecurityUtils;
import com.maketo.utilisateur.dto.MotDePasseRequest;
import com.maketo.utilisateur.dto.ProfilRequest;
import com.maketo.utilisateur.dto.UtilisateurResponse;
import com.maketo.utilisateur.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping("/moi")
    public UtilisateurResponse moi(@AuthenticationPrincipal Jwt jwt) {
        return utilisateurService.profil(SecurityUtils.idUtilisateur(jwt));
    }

    @PutMapping("/moi")
    public UtilisateurResponse modifierProfil(@Valid @RequestBody ProfilRequest requete,
                                              @AuthenticationPrincipal Jwt jwt) {
        return utilisateurService.modifierProfil(SecurityUtils.idUtilisateur(jwt), requete);
    }

    /** Après un changement, toutes les sessions sont coupées : le front doit reconnecter l'utilisateur. */
    @PatchMapping("/moi/mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(@Valid @RequestBody MotDePasseRequest requete,
                                                  @AuthenticationPrincipal Jwt jwt) {
        utilisateurService.changerMotDePasse(SecurityUtils.idUtilisateur(jwt), requete);
        return ResponseEntity.noContent().build();
    }
}
