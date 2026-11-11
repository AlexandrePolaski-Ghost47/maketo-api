package com.maketo.credit.dto;

import com.maketo.credit.entity.TypeTransaction;

import java.time.Instant;

public record TransactionResponse(Long id, TypeTransaction type, int montant, int soldeApres, String libelle,
                                  Long annonceId, Instant date) {
}
