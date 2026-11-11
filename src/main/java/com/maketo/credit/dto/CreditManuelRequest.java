package com.maketo.credit.dto;

import com.maketo.credit.entity.TypeTransaction;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Geste commercial (BONUS) ou remboursement (REMBOURSEMENT) accordé par l'admin. */
public record CreditManuelRequest(

        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être positif")
        @Max(value = 100_000, message = "Montant trop élevé")
        Integer montant,

        @NotNull(message = "Le type est obligatoire (BONUS ou REMBOURSEMENT)")
        TypeTransaction type
) {
}
