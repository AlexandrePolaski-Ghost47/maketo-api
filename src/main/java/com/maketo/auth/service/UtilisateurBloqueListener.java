package com.maketo.auth.service;

import com.maketo.utilisateur.event.MotDePasseModifieEvent;
import com.maketo.utilisateur.event.UtilisateurBloqueEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Coupe toutes les sessions d'un utilisateur quand son compte est bloqué ou que son mot de passe change
 * (un éventuel voleur de session est déconnecté).
 */
@Component
@RequiredArgsConstructor
public class UtilisateurBloqueListener {

    private final RefreshTokenService refreshTokenService;

    @EventListener
    public void surBlocage(UtilisateurBloqueEvent evenement) {
        refreshTokenService.revoquerTous(evenement.utilisateurId());
    }

    @EventListener
    public void surChangementMotDePasse(MotDePasseModifieEvent evenement) {
        refreshTokenService.revoquerTous(evenement.utilisateurId());
    }
}
