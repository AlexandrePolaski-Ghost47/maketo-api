package com.maketo.auth.controller;

import com.maketo.auth.dto.AuthResponse;
import com.maketo.auth.dto.ConnexionRequest;
import com.maketo.auth.dto.InscriptionRequest;
import com.maketo.auth.service.AuthService;
import com.maketo.auth.service.SessionAuthentifiee;
import com.maketo.config.MaketoProperties;
import com.maketo.utilisateur.dto.UtilisateurResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String COOKIE_REFRESH = "maketo_refresh";
    private static final String CHEMIN_COOKIE = "/api/auth";

    private final AuthService authService;
    private final MaketoProperties properties;

    @PostMapping("/inscription")
    public ResponseEntity<UtilisateurResponse> inscription(@Valid @RequestBody InscriptionRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrire(requete));
    }

    @PostMapping("/connexion")
    public ResponseEntity<AuthResponse> connexion(@Valid @RequestBody ConnexionRequest requete) {
        return avecCookie(authService.connecter(requete));
    }

    @PostMapping("/rafraichir")
    public ResponseEntity<AuthResponse> rafraichir(
            @CookieValue(name = COOKIE_REFRESH, required = false) String refreshToken) {
        return avecCookie(authService.rafraichir(refreshToken));
    }

    @PostMapping("/deconnexion")
    public ResponseEntity<Void> deconnexion(
            @CookieValue(name = COOKIE_REFRESH, required = false) String refreshToken) {
        authService.deconnecter(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseEntity<AuthResponse> avecCookie(SessionAuthentifiee session) {
        Duration duree = Duration.ofDays(properties.security().refreshTokenDays());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie(session.refreshToken(), duree).toString())
                .body(session.reponse());
    }

    /** Cookie httpOnly (illisible en JavaScript, donc protégé contre le vol par XSS), limité à /api/auth. */
    private ResponseCookie cookie(String valeur, Duration duree) {
        MaketoProperties.Security securite = properties.security();
        return ResponseCookie.from(COOKIE_REFRESH, valeur)
                .httpOnly(true)
                .secure(securite.cookieSecure())
                .sameSite(securite.cookieSameSite())
                .path(CHEMIN_COOKIE)
                .maxAge(duree)
                .build();
    }
}
