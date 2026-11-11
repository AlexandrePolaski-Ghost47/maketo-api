package com.maketo.annonce.dto;

/** Lien WhatsApp avec message pré-rempli + numéro pour un appel direct. */
public record ContactResponse(String lienWhatsapp, String telephone) {
}
