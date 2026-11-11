package com.maketo.common.util;

/**
 * Numéros camerounais : accepte "6XXXXXXXX", "6XX XX XX XX", "+237 6XXXXXXXX"...
 * et les stocke toujours au format "6XXXXXXXX".
 */
public final class TelephoneUtils {

    public static final String REGEX = "^(\\+?237)?[ ]?6([ ]?[0-9]){8}$";

    private TelephoneUtils() {
    }

    public static String normaliser(String telephone) {
        if (telephone == null) {
            return null;
        }
        String chiffres = telephone.replaceAll("[^0-9]", "");
        if (chiffres.length() == 12 && chiffres.startsWith("237")) {
            chiffres = chiffres.substring(3);
        }
        return chiffres;
    }
}
