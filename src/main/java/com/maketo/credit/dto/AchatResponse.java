package com.maketo.credit.dto;

import java.time.Instant;

/** @param jusqua fin du boost ou de l'abonnement Pro */
public record AchatResponse(int soldeRestant, Instant jusqua) {
}
