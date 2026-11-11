package com.maketo.publicite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Champs du formulaire multipart (l'image est envoyée à part, champ "image"). */
public record PubliciteRequest(

        @NotBlank(message = "L'annonceur est obligatoire")
        @Size(max = 100, message = "100 caractères maximum")
        String annonceur,

        @Size(max = 100, message = "100 caractères maximum")
        String titre,

        @Pattern(regexp = "^https?://.{3,490}$", message = "Le lien doit commencer par http:// ou https://")
        String lien,

        @NotNull(message = "La date de début est obligatoire")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateDebut,

        @NotNull(message = "La date de fin est obligatoire")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateFin
) {
}
