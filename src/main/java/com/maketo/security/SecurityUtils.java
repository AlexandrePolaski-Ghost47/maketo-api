package com.maketo.security;

import com.maketo.config.SecurityConfig;
import com.maketo.utilisateur.entity.Role;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** L'id de l'utilisateur connecté est le "subject" de son JWT. */
    public static Long idUtilisateur(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    /** Pour les routes publiques : null si le visiteur n'est pas connecté. */
    public static Long idUtilisateurOuNull(Jwt jwt) {
        return jwt == null ? null : idUtilisateur(jwt);
    }

    public static boolean estAdmin(Jwt jwt) {
        return jwt != null && Role.ADMIN.name().equals(jwt.getClaimAsString(SecurityConfig.CLAIM_ROLE));
    }
}
