package com.maketo.signalement.dto;

import com.maketo.signalement.entity.MotifSignalement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignalementRequest(

        @NotNull(message = "Choisis un motif")
        MotifSignalement motif,

        @Size(max = 500, message = "500 caractères maximum")
        String commentaire
) {
}
