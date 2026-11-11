package com.maketo.security;

import com.maketo.config.JwtConfig;
import com.maketo.config.MaketoProperties;
import com.maketo.config.SecurityConfig;
import com.maketo.utilisateur.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Génère les jetons d'accès (courte durée). */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final MaketoProperties properties;
    private final Clock clock;

    public JetonAcces genererJetonAcces(Utilisateur utilisateur) {
        Instant maintenant = clock.instant();
        Duration duree = Duration.ofMinutes(properties.security().accessTokenMinutes());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.EMETTEUR)
                .issuedAt(maintenant)
                .expiresAt(maintenant.plus(duree))
                .subject(utilisateur.getId().toString())
                .claim(SecurityConfig.CLAIM_ROLE, utilisateur.getRole().name())
                .build();

        JwsHeader entete = JwsHeader.with(MacAlgorithm.HS256).build();
        String valeur = jwtEncoder.encode(JwtEncoderParameters.from(entete, claims)).getTokenValue();
        return new JetonAcces(valeur, duree.toSeconds());
    }

    public record JetonAcces(String valeur, long expireDansSecondes) {
    }
}
