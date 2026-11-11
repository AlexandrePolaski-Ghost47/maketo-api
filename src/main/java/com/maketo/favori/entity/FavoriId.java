package com.maketo.favori.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Clé composée (utilisateur, annonce). */
@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriId implements Serializable {

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(name = "annonce_id")
    private Long annonceId;
}
