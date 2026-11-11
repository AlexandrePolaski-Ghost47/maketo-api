package com.maketo.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Écrit une erreur au format problem+json dans les filtres de sécurité (hors contrôleurs). */
final class ReponseJson {

    private ReponseJson() {
    }

    static void ecrire(HttpServletResponse response, int status, String titre, String detail) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/problem+json");
        response.getWriter().write(
                "{\"status\":" + status + ",\"title\":\"" + titre + "\",\"detail\":\"" + detail + "\"}");
    }
}
