package com.maketo.credit.dto;

import jakarta.validation.constraints.Size;

/** @param offreCode optionnel : offre par défaut (BOOST_24H ou PRO_MENSUEL) si absent */
public record AchatRequest(@Size(max = 30) String offreCode) {
}
