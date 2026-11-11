package com.maketo.categorie.entity;

import com.maketo.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categorie")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Categorie extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String nom;

    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(length = 50)
    private String icone;

    @Column(name = "ordre_affichage", nullable = false)
    private Integer ordreAffichage = 0;

    @Column(nullable = false)
    private Boolean actif = Boolean.TRUE;

    public static Categorie creer(String nom, String slug, String icone, int ordreAffichage) {
        Categorie c = new Categorie();
        c.nom = nom;
        c.slug = slug;
        c.icone = icone;
        c.ordreAffichage = ordreAffichage;
        return c;
    }
}
