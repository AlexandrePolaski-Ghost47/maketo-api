package com.maketo.credit.dto;

import java.time.Instant;

public record PortefeuilleResponse(int solde, boolean pro, Instant proExpireLe, boolean paiementsActifs) {
}
