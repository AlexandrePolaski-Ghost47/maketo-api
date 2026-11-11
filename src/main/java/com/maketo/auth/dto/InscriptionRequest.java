package com.maketo.auth.dto;

import com.maketo.common.util.TelephoneUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InscriptionRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 80, message = "80 caractères maximum")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 80, message = "80 caractères maximum")
        String prenom,

        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = TelephoneUtils.REGEX, message = "Numéro camerounais invalide (ex : 6XXXXXXXX)")
        String telephone,

        @Pattern(regexp = TelephoneUtils.REGEX, message = "Numéro WhatsApp invalide (ex : 6XXXXXXXX)")
        String whatsapp,

        @NotBlank(message = "Le matricule est obligatoire")
        @Pattern(regexp = "^[A-Za-z0-9-]{3,30}$", message = "Matricule invalide (lettres, chiffres, tiret)")
        String matricule,

        @Size(max = 100, message = "100 caractères maximum")
        String filiere,

        @Size(max = 20, message = "20 caractères maximum")
        String niveau,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit faire entre 8 et 72 caractères")
        String motDePasse
) {
}
