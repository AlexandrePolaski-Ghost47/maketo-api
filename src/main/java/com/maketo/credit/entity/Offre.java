package com.maketo.credit.entity;

import com.maketo.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;

/**
 * Produit payant (boost, abonnement...). Ajouter une offre = insérer une ligne, sans code.
 */
@Entity
@Table(name = "offre")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Offre extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Setter
    @Column(nullable = false, length = 100)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeOffre type;

    @Setter
    @Column(nullable = false)
    private Integer prix;

    @Setter
    @Column(name = "duree_heures", nullable = false)
    private Integer dureeHeures;

    @Setter
    @Column(nullable = false)
    private Boolean actif = Boolean.TRUE;

    public Duration getDuree() {
        return Duration.ofHours(dureeHeures);
    }
}
