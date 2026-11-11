package com.maketo.annonce.dto;

import org.springframework.data.domain.Sort;

/** Tris autorisés (liste fermée : le client ne peut pas trier sur n'importe quel champ). */
public enum TriAnnonce {
    RECENT(Sort.by(Sort.Direction.DESC, "dateCreation")),
    PRIX_CROISSANT(Sort.by(Sort.Direction.ASC, "prix").and(Sort.by(Sort.Direction.DESC, "dateCreation"))),
    PRIX_DECROISSANT(Sort.by(Sort.Direction.DESC, "prix").and(Sort.by(Sort.Direction.DESC, "dateCreation")));

    private final Sort sort;

    TriAnnonce(Sort sort) {
        this.sort = sort;
    }

    public Sort sort() {
        return sort;
    }
}
