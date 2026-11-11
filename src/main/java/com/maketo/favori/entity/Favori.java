package com.maketo.favori.entity;

import com.maketo.annonce.entity.Annonce;
import com.maketo.utilisateur.entity.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "favori")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favori {

    @EmbeddedId
    private FavoriId id;

    @MapsId("utilisateurId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @MapsId("annonceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id")
    private Annonce annonce;

    @CreationTimestamp
    @Column(name = "date_ajout", nullable = false, updatable = false)
    private Instant dateAjout;

    public static Favori ajouter(Utilisateur utilisateur, Annonce annonce) {
        Favori f = new Favori();
        f.id = new FavoriId(utilisateur.getId(), annonce.getId());
        f.utilisateur = utilisateur;
        f.annonce = annonce;
        return f;
    }
}
