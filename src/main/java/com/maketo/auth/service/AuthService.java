package com.maketo.auth.service;

import com.maketo.auth.dto.ConnexionRequest;
import com.maketo.auth.dto.InscriptionRequest;
import com.maketo.utilisateur.dto.UtilisateurResponse;

public interface AuthService {

    UtilisateurResponse inscrire(InscriptionRequest requete);

    SessionAuthentifiee connecter(ConnexionRequest requete);

    SessionAuthentifiee rafraichir(String refreshToken);

    void deconnecter(String refreshToken);
}
