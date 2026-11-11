package com.maketo.publicite.entity;

import com.maketo.common.entity.BaseEntity;
import com.maketo.common.exception.MetierException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/** Bannière payée par une boutique d'Ambam, affichée pendant une période (dates incluses). */
@Entity
@Table(name = "publicite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Publicite extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String annonceur;

    @Column(length = 100)
    private String titre;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_id", nullable = false, length = 200)
    private String imageId;

    @Column(length = 500)
    private String lien;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private Boolean actif = Boolean.TRUE;

    @Column(name = "nb_clics", nullable = false)
    private Integer nbClics = 0;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    public static Publicite creer(String annonceur, String titre, String imageUrl, String imageId, String lien,
                                  LocalDate dateDebut, LocalDate dateFin) {
        if (dateFin.isBefore(dateDebut)) {
            throw new MetierException("La date de fin doit être après la date de début");
        }
        Publicite p = new Publicite();
        p.annonceur = annonceur;
        p.titre = titre;
        p.imageUrl = imageUrl;
        p.imageId = imageId;
        p.lien = lien;
        p.dateDebut = dateDebut;
        p.dateFin = dateFin;
        return p;
    }

    public void activer() {
        this.actif = Boolean.TRUE;
    }

    public void desactiver() {
        this.actif = Boolean.FALSE;
    }
}
