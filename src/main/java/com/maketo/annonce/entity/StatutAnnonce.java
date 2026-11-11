package com.maketo.annonce.entity;

public enum StatutAnnonce {
    ACTIVE,
    VENDUE,
    EXPIREE,
    SUPPRIMEE,
    /** Masquée automatiquement après trop de signalements, en attente de modération. */
    SUSPENDUE
}
