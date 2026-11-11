package com.maketo.annonce.dto;

/** Vendeur tel qu'affiché sur une annonce : le numéro n'est donné qu'aux étudiants connectés. */
public record VendeurResponse(
        Long id,
        String nomAffiche,
        String filiere,
        boolean pro,
        String whatsapp
) {
}
