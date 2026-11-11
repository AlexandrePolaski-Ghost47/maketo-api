package com.maketo.credit.dto;

import com.maketo.common.util.TelephoneUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RechargeRequest(

        @NotNull(message = "Choisis un montant")
        Integer montant,

        @NotBlank(message = "Choisis ton opérateur")
        @Pattern(regexp = "^[A-Za-z_]{2,20}$", message = "Opérateur invalide")
        String operateur,

        @NotBlank(message = "Le numéro est obligatoire")
        @Pattern(regexp = TelephoneUtils.REGEX, message = "Numéro camerounais invalide (ex : 6XXXXXXXX)")
        String telephone
) {
}
