package com.maketo.utilisateur.event;

/**
 * Publié quand un admin bloque un compte. Les autres modules réagissent
 * (ex. auth révoque les sessions) sans que le module utilisateur les connaisse.
 */
public record UtilisateurBloqueEvent(Long utilisateurId) {
}
