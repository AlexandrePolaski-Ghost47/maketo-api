package com.maketo.utilisateur.dto;

import com.maketo.common.util.TelephoneUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Champs modifiables par l'étudiant (téléphone et matricule ne changent pas : ils identifient le compte). */
public record ProfilRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 80, message = "80 caractères maximum")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 80, message = "80 caractères maximum")
        String prenom,

        @Pattern(regexp = TelephoneUtils.REGEX, message = "Numéro WhatsApp invalide (ex : 6XXXXXXXX)")
        String whatsapp,

        @Size(max = 100, message = "100 caractères maximum")
        String filiere,

        @Size(max = 20, message = "20 caractères maximum")
        String niveau
) {
}
