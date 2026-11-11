package com.maketo.annonce.entity;

import com.maketo.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Photo d'une annonce. Fait partie de l'agrégat Annonce :
 * elle n'est créée/supprimée qu'à travers l'annonce (pas de repository dédié).
 */
@Entity
@Table(name = "photo_annonce")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoAnnonce extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "annonce_id", nullable = false)
    private Annonce annonce;

    @Column(nullable = false, length = 500)
    private String url;

    /** Identifiant chez l'hébergeur d'images (Cloudinary), utile pour la suppression. */
    @Column(name = "public_id", nullable = false, length = 200)
    private String publicId;

    @Column(nullable = false)
    private Short ordre;

    PhotoAnnonce(Annonce annonce, String url, String publicId, short ordre) {
        this.annonce = annonce;
        this.url = url;
        this.publicId = publicId;
        this.ordre = ordre;
    }

    void changerOrdre(short nouvelOrdre) {
        this.ordre = nouvelOrdre;
    }
}
