package com.maketo.signalement.entity;

import com.maketo.annonce.entity.Annonce;
import com.maketo.common.entity.BaseEntity;
import com.maketo.utilisateur.entity.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "signalement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Signalement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotifSignalement motif;

    @Column(length = 500)
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSignalement statut = StatutSignalement.OUVERT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traite_par")
    private Utilisateur traitePar;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_traitement")
    private Instant dateTraitement;

    public static Signalement signaler(Annonce annonce, Utilisateur auteur, MotifSignalement motif, String commentaire) {
        Signalement s = new Signalement();
        s.annonce = annonce;
        s.auteur = auteur;
        s.motif = motif;
        s.commentaire = commentaire;
        return s;
    }

    public void traiter(Utilisateur admin, Instant maintenant) {
        this.statut = StatutSignalement.TRAITE;
        this.traitePar = admin;
        this.dateTraitement = maintenant;
    }
}
