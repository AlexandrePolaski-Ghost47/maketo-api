package com.maketo.favori.dto;

import com.maketo.annonce.dto.AnnonceResumeResponse;

import java.time.Instant;

/** @param disponible false si l'annonce a été vendue, a expiré ou est masquée depuis l'ajout en favori */
public record FavoriResponse(AnnonceResumeResponse annonce, boolean disponible, Instant dateAjout) {
}
