package com.maketo.parametre.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Réglage modifiable depuis l'admin (durées, quota...) sans redéployer l'appli.
 */
@Entity
@Table(name = "parametre")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parametre {

    @Id
    @Column(length = 60)
    private String cle;

    @Column(nullable = false)
    private String valeur;

    @Column
    private String description;

    @UpdateTimestamp
    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    public void modifierValeur(String nouvelleValeur) {
        this.valeur = nouvelleValeur;
    }
}
