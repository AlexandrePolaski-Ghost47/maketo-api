package com.maketo.categorie.dto;

public record CategorieAdminResponse(Long id, String nom, String slug, String icone, Integer ordreAffichage,
                                     Boolean actif) {
}
