package com.maketo.annonce.dto;

import java.time.Instant;

/** Page publique d'un vendeur. */
public record VitrineResponse(VendeurResponse vendeur, Instant membreDepuis, long nbAnnoncesEnLigne) {
}
