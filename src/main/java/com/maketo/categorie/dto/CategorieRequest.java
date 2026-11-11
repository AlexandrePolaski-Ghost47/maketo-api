package com.maketo.categorie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CategorieRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 60, message = "60 caractères maximum")
        String nom,

        @NotBlank(message = "Le slug est obligatoire")
        @Pattern(regexp = "^[a-z0-9-]{2,60}$", message = "Slug : minuscules, chiffres et tirets uniquement")
        String slug,

        @Size(max = 50, message = "50 caractères maximum")
        String icone,

        @NotNull(message = "L'ordre d'affichage est obligatoire")
        @PositiveOrZero(message = "L'ordre doit être positif")
        Integer ordreAffichage
) {
}
