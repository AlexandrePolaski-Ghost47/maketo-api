package com.maketo.parametre.dto;

import com.maketo.parametre.entity.TypeParametre;

import java.time.Instant;

public record ParametreResponse(String cle, String valeur, TypeParametre type, String description,
                                Instant dateModification) {
}
