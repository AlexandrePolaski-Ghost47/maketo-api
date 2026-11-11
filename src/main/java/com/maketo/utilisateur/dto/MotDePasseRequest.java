package com.maketo.utilisateur.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotDePasseRequest(

        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        String ancien,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit faire entre 8 et 72 caractères")
        String nouveau
) {
}
