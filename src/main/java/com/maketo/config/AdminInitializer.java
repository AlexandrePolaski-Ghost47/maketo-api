package com.maketo.config;

import com.maketo.common.util.TelephoneUtils;
import com.maketo.utilisateur.entity.Utilisateur;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crée le compte administrateur au démarrage s'il n'existe pas encore.
 * Les identifiants viennent de la configuration (variables d'environnement en production),
 * jamais d'un script SQL versionné.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final MaketoProperties properties;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        MaketoProperties.Admin admin = properties.admin();
        if (admin == null || estVide(admin.telephone()) || estVide(admin.motDePasse())) {
            log.info("Aucun compte admin configuré (maketo.admin.*) : création ignorée");
            return;
        }
        if (admin.motDePasse().length() < 8) {
            throw new IllegalStateException("maketo.admin.mot-de-passe doit faire au moins 8 caractères");
        }

        String telephone = TelephoneUtils.normaliser(admin.telephone());
        if (utilisateurRepository.existsByTelephone(telephone)) {
            log.info("Compte admin déjà présent ({})", telephone);
            return;
        }

        Utilisateur utilisateur = Utilisateur.inscrire(
                valeurOuDefaut(admin.nom(), "Admin"),
                valeurOuDefaut(admin.prenom(), "Maketo"),
                telephone, telephone,
                "ADMIN-" + telephone,
                null, null,
                passwordEncoder.encode(admin.motDePasse()));
        utilisateur.promouvoirAdmin();
        utilisateurRepository.save(utilisateur);
        log.info("Compte admin créé ({})", telephone);
    }

    private static boolean estVide(String s) {
        return s == null || s.isBlank();
    }

    private static String valeurOuDefaut(String s, String defaut) {
        return estVide(s) ? defaut : s;
    }
}
