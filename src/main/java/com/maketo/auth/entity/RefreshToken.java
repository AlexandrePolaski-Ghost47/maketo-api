package com.maketo.auth.entity;

import com.maketo.common.entity.BaseEntity;
import com.maketo.utilisateur.entity.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Jeton de rafraîchissement. Seul son empreinte SHA-256 est stockée :
 * une fuite de la base ne permet pas de se connecter.
 */
@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expire_le", nullable = false)
    private Instant expireLe;

    @Column(nullable = false)
    private Boolean revoque = Boolean.FALSE;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    public static RefreshToken emettre(Utilisateur utilisateur, String tokenHash, Instant expireLe) {
        RefreshToken t = new RefreshToken();
        t.utilisateur = utilisateur;
        t.tokenHash = tokenHash;
        t.expireLe = expireLe;
        return t;
    }

    public boolean estValide(Instant maintenant) {
        return !revoque && expireLe.isAfter(maintenant);
    }

    public void revoquer() {
        this.revoque = Boolean.TRUE;
    }
}
