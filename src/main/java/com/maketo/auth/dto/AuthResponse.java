package com.maketo.auth.dto;

import com.maketo.utilisateur.dto.UtilisateurResponse;

/** Le refresh token n'est PAS dans le corps : il voyage dans un cookie httpOnly. */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expireDansSecondes,
        UtilisateurResponse utilisateur
) {
}
