package com.maketo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Réponse JSON (RFC 9457) quand le jeton est absent, invalide ou expiré : HTTP 401. */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ReponseJson.ecrire(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Non authentifié", "Connecte-toi pour accéder à cette ressource");
    }
}
