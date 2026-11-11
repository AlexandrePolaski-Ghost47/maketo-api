package com.maketo.common.util;

import java.time.ZoneId;

/** Fuseau horaire de référence pour les règles calendaires (début de mois, fin de période gratuite). */
public final class Fuseau {

    public static final ZoneId CAMEROUN = ZoneId.of("Africa/Douala");

    private Fuseau() {
    }
}
