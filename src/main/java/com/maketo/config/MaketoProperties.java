package com.maketo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Toute la configuration propre à Maketo (préfixe "maketo" dans application.yaml).
 */
@ConfigurationProperties(prefix = "maketo")
public record MaketoProperties(Security security, Cors cors, Admin admin, Stockage stockage, Paiement paiement) {

    public record Security(String jwtSecret,
                           int accessTokenMinutes,
                           int refreshTokenDays,
                           boolean cookieSecure,
                           String cookieSameSite) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    /** Compte administrateur créé au premier démarrage (valeurs venant de variables d'environnement). */
    public record Admin(String telephone, String motDePasse, String nom, String prenom) {
    }

    /** Où sont stockées les photos : "local" (développement) ou "cloudinary" (production). */
    public record Stockage(String type, Local local, Cloudinary cloudinary) {

        public record Local(String dossier, String urlBase) {
        }

        public record Cloudinary(String cloudName, String apiKey, String apiSecret, String dossier) {
        }
    }

    /**
     * Paiements Mobile Money.
     *
     * @param fournisseur "simulation" (développement, aucun argent réel) ou "ctpay"
     * @param callbackUrl URL publique du webhook (vide en local : on s'appuie alors sur la vérification de statut)
     */
    public record Paiement(String fournisseur, String callbackUrl, CtPay ctpay) {

        /** @param prefixeTelephone ajouté devant le numéro "6XXXXXXXX" (vide par défaut) */
        public record CtPay(String urlBase, String token, String prefixeTelephone) {
        }
    }
}
