package com.maketo.parametre.service;

import com.maketo.parametre.dto.ParametreResponse;
import com.maketo.parametre.entity.ParametreCle;

import java.time.LocalDate;
import java.util.List;

/** Accès typé aux réglages de l'appli stockés en base. */
public interface ParametreService {

    String texte(ParametreCle cle);

    int entier(ParametreCle cle);

    LocalDate date(ParametreCle cle);

    boolean booleen(ParametreCle cle);

    List<Integer> listeEntiers(ParametreCle cle);

    List<ParametreResponse> lister();

    /** Valide le format (selon le type du paramètre) puis enregistre. */
    ParametreResponse modifier(String cle, String valeur);
}
