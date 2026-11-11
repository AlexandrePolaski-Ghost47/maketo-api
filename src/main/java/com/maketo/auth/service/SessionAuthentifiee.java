package com.maketo.auth.service;

import com.maketo.auth.dto.AuthResponse;

/** Résultat interne d'une connexion : la réponse JSON + le refresh token brut à mettre en cookie. */
public record SessionAuthentifiee(AuthResponse reponse, String refreshToken) {
}
