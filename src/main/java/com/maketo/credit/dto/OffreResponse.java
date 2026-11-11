package com.maketo.credit.dto;

import com.maketo.credit.entity.TypeOffre;

public record OffreResponse(String code, String libelle, TypeOffre type, int prix, int dureeHeures) {
}
