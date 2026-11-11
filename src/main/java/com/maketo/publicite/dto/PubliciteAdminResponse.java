package com.maketo.publicite.dto;

import java.time.LocalDate;

public record PubliciteAdminResponse(Long id, String annonceur, String titre, String imageUrl, String lien,
                                     LocalDate dateDebut, LocalDate dateFin, boolean actif, boolean enCours,
                                     int nbClics) {
}
