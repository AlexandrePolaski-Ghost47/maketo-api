package com.maketo.parametre.entity;

import java.util.Arrays;
import java.util.Optional;

/**
 * Clés connues de la table parametre : évite les fautes de frappe dans le code
 * et indique le format de chaque valeur.
 */
public enum ParametreCle {
    DUREE_ANNONCE_GRATUITE_JOURS("duree_annonce_gratuite_jours", TypeParametre.ENTIER),
    DUREE_ANNONCE_PRO_JOURS("duree_annonce_pro_jours", TypeParametre.ENTIER),
    QUOTA_GRATUIT_MENSUEL("quota_gratuit_mensuel", TypeParametre.ENTIER),
    NB_MAX_PHOTOS("nb_max_photos", TypeParametre.ENTIER),
    DATE_FIN_PERIODE_GRATUITE("date_fin_periode_gratuite", TypeParametre.DATE),
    SEUIL_SUSPENSION_SIGNALEMENTS("seuil_suspension_signalements", TypeParametre.ENTIER),
    PAIEMENTS_ACTIFS("paiements_actifs", TypeParametre.BOOLEEN),
    MONTANTS_RECHARGE("montants_recharge", TypeParametre.LISTE_ENTIERS);

    private final String cle;
    private final TypeParametre type;

    ParametreCle(String cle, TypeParametre type) {
        this.cle = cle;
        this.type = type;
    }

    public String getCle() {
        return cle;
    }

    public TypeParametre getType() {
        return type;
    }

    public static Optional<ParametreCle> depuis(String cle) {
        return Arrays.stream(values()).filter(c -> c.cle.equals(cle)).findFirst();
    }
}
