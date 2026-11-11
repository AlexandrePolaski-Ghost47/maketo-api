package com.maketo.auth.service;

import com.maketo.auth.entity.RefreshToken;
import com.maketo.auth.repository.RefreshTokenRepository;
import com.maketo.common.exception.SessionInvalideException;
import com.maketo.config.MaketoProperties;
import com.maketo.utilisateur.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh tokens avec rotation : chaque utilisation en délivre un nouveau et révoque l'ancien.
 * Si un jeton déjà révoqué est réutilisé (signe de vol), toutes les sessions de l'utilisateur sont coupées.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom ALEATOIRE = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final MaketoProperties properties;
    private final Clock clock;

    /** Crée un jeton et renvoie sa valeur brute (seule l'empreinte est stockée). */
    @Transactional
    public String emettre(Utilisateur utilisateur) {
        byte[] octets = new byte[32];
        ALEATOIRE.nextBytes(octets);
        String brut = Base64.getUrlEncoder().withoutPadding().encodeToString(octets);

        Instant expiration = clock.instant().plus(Duration.ofDays(properties.security().refreshTokenDays()));
        refreshTokenRepository.save(RefreshToken.emettre(utilisateur, empreinte(brut), expiration));
        return brut;
    }

    /** Vérifie le jeton, le révoque et le renvoie. Lève SessionInvalideException s'il n'est pas utilisable. */
    @Transactional(noRollbackFor = SessionInvalideException.class)
    public RefreshToken consommer(String brut) {
        RefreshToken jeton = refreshTokenRepository.findByTokenHash(empreinte(brut))
                .orElseThrow(SessionInvalideException::new);

        if (Boolean.TRUE.equals(jeton.getRevoque())) {
            Long utilisateurId = jeton.getUtilisateur().getId();
            log.warn("Réutilisation d'un refresh token révoqué : sessions de l'utilisateur {} coupées", utilisateurId);
            refreshTokenRepository.revoquerTousPourUtilisateur(utilisateurId);
            throw new SessionInvalideException();
        }
        if (!jeton.estValide(clock.instant())) {
            throw new SessionInvalideException();
        }
        jeton.revoquer();
        return jeton;
    }

    @Transactional
    public void revoquer(String brut) {
        refreshTokenRepository.findByTokenHash(empreinte(brut)).ifPresent(RefreshToken::revoquer);
    }

    @Transactional
    public void revoquerTous(Long utilisateurId) {
        refreshTokenRepository.revoquerTousPourUtilisateur(utilisateurId);
    }

    private static String empreinte(String brut) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(brut.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
