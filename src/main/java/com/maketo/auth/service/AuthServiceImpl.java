package com.maketo.auth.service;

import com.maketo.auth.dto.AuthResponse;
import com.maketo.auth.dto.ConnexionRequest;
import com.maketo.auth.dto.InscriptionRequest;
import com.maketo.auth.entity.RefreshToken;
import com.maketo.common.exception.CompteNonActifException;
import com.maketo.common.exception.ConflitException;
import com.maketo.common.exception.IdentifiantsInvalidesException;
import com.maketo.common.exception.SessionInvalideException;
import com.maketo.common.util.TelephoneUtils;
import com.maketo.security.JwtService;
import com.maketo.security.ProtectionConnexionService;
import com.maketo.utilisateur.dto.UtilisateurResponse;
import com.maketo.utilisateur.entity.StatutUtilisateur;
import com.maketo.utilisateur.entity.Utilisateur;
import com.maketo.utilisateur.mapper.UtilisateurMapper;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ProtectionConnexionService protectionConnexion;
    private final UtilisateurMapper utilisateurMapper;

    /** Hash factice : même temps de réponse que le numéro existe ou non (anti-énumération). */
    private final String hashFactice;

    public AuthServiceImpl(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService, RefreshTokenService refreshTokenService,
                           ProtectionConnexionService protectionConnexion, UtilisateurMapper utilisateurMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.protectionConnexion = protectionConnexion;
        this.utilisateurMapper = utilisateurMapper;
        this.hashFactice = passwordEncoder.encode("mot-de-passe-factice-maketo");
    }

    @Override
    @Transactional
    public UtilisateurResponse inscrire(InscriptionRequest r) {
        String telephone = TelephoneUtils.normaliser(r.telephone());
        String matricule = r.matricule().trim().toUpperCase();

        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new ConflitException("Ce numéro est déjà utilisé");
        }
        if (utilisateurRepository.existsByMatricule(matricule)) {
            throw new ConflitException("Ce matricule est déjà utilisé");
        }

        Utilisateur utilisateur = Utilisateur.inscrire(
                r.nom().trim(), r.prenom().trim(), telephone,
                TelephoneUtils.normaliser(r.whatsapp()), matricule,
                r.filiere(), r.niveau(),
                passwordEncoder.encode(r.motDePasse()));

        return utilisateurMapper.versResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    @Transactional
    public SessionAuthentifiee connecter(ConnexionRequest r) {
        String telephone = TelephoneUtils.normaliser(r.telephone());
        protectionConnexion.verifierNonBloque(telephone);

        Optional<Utilisateur> trouve = utilisateurRepository.findByTelephone(telephone);
        String hash = trouve.map(Utilisateur::getMotDePasse).orElse(hashFactice);
        boolean motDePasseCorrect = passwordEncoder.matches(r.motDePasse(), hash);

        if (trouve.isEmpty() || !motDePasseCorrect) {
            protectionConnexion.enregistrerEchec(telephone);
            throw new IdentifiantsInvalidesException();
        }
        protectionConnexion.reinitialiser(telephone);

        Utilisateur utilisateur = trouve.get();
        verifierCompteActif(utilisateur);
        return ouvrirSession(utilisateur);
    }

    @Override
    @Transactional(noRollbackFor = SessionInvalideException.class)
    public SessionAuthentifiee rafraichir(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new SessionInvalideException();
        }
        RefreshToken ancien = refreshTokenService.consommer(refreshToken);
        Utilisateur utilisateur = ancien.getUtilisateur();
        verifierCompteActif(utilisateur);
        return ouvrirSession(utilisateur);
    }

    @Override
    @Transactional
    public void deconnecter(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoquer(refreshToken);
        }
    }

    private void verifierCompteActif(Utilisateur utilisateur) {
        if (utilisateur.getStatut() == StatutUtilisateur.EN_ATTENTE) {
            throw new CompteNonActifException("Ton compte est en attente de validation par l'administrateur");
        }
        if (utilisateur.getStatut() == StatutUtilisateur.BLOQUE) {
            throw new CompteNonActifException("Ton compte a été bloqué. Contacte l'administrateur");
        }
    }

    private SessionAuthentifiee ouvrirSession(Utilisateur utilisateur) {
        JwtService.JetonAcces jeton = jwtService.genererJetonAcces(utilisateur);
        String refreshToken = refreshTokenService.emettre(utilisateur);
        AuthResponse reponse = new AuthResponse(jeton.valeur(), "Bearer", jeton.expireDansSecondes(),
                utilisateurMapper.versResponse(utilisateur));
        return new SessionAuthentifiee(reponse, refreshToken);
    }
}
