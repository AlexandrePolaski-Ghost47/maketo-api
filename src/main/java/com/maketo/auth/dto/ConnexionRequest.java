package com.maketo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ConnexionRequest(

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        String telephone,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse
) {
}
