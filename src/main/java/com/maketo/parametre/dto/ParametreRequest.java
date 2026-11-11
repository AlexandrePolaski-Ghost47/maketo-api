package com.maketo.parametre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParametreRequest(
        @NotBlank(message = "La valeur est obligatoire")
        @Size(max = 255, message = "255 caractères maximum")
        String valeur
) {
}
