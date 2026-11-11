package com.maketo.admin;

/** Tableau de bord de l'administrateur. Montants en FCFA ; "mois" = mois calendaire en cours (heure du Cameroun). */
public record StatistiquesResponse(
        long utilisateursActifs,
        long inscriptionsEnAttente,
        long nouveauxInscrits7Jours,
        long annoncesEnLigne,
        long annoncesPubliees7Jours,
        long annoncesVendues,
        long annoncesSuspendues,
        long signalementsOuverts,
        long encaisseCeMois,
        long fraisCeMois,
        boolean paiementsActifs
) {
}
