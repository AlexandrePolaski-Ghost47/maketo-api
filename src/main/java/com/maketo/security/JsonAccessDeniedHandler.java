package com.maketo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Réponse JSON quand l'utilisateur est connecté mais n'a pas le droit : HTTP 403. */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ReponseJson.ecrire(response, HttpServletResponse.SC_FORBIDDEN,
                "Accès refusé", "Tu n'as pas les droits pour cette action");
    }
}
