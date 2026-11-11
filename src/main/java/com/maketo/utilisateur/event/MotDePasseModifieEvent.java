package com.maketo.utilisateur.event;

/** Publié après un changement de mot de passe : toutes les sessions existantes sont coupées. */
public record MotDePasseModifieEvent(Long utilisateurId) {
}
