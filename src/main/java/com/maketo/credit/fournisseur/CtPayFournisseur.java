package com.maketo.credit.fournisseur;

import com.maketo.config.MaketoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intégration CT PAY (Connect Technology) : https://gateway-prod.connecttechnology.io
 * Le token marchand ne quitte jamais le serveur.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "maketo.paiement", name = "fournisseur", havingValue = "ctpay")
public class CtPayFournisseur implements FournisseurPaiement {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {
    };
    private static final Pattern MESSAGE = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"");

    private final RestClient client;
    private final String prefixeTelephone;

    public CtPayFournisseur(MaketoProperties properties) {
        MaketoProperties.Paiement.CtPay config = properties.paiement().ctpay();
        if (config == null || vide(config.token()) || vide(config.urlBase())) {
            throw new IllegalStateException("Configuration CT Pay incomplète (maketo.paiement.ctpay.*)");
        }
        JdkClientHttpRequestFactory http = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        http.setReadTimeout(Duration.ofSeconds(30));

        this.client = RestClient.builder()
                .baseUrl(config.urlBase())
                .requestFactory(http)
                .defaultHeader("Token", config.token())
                .build();
        this.prefixeTelephone = config.prefixeTelephone() == null ? "" : config.prefixeTelephone().trim();
    }

    @Override
    public String code() {
        return "CTPAY";
    }

    @Override
    public List<OperateurPaiement> operateurs() {
        Map<String, Object> reponse = client.get().uri("/ct-payment/api/operator/getAll")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JSON);
        List<OperateurPaiement> operateurs = new ArrayList<>();
        if (reponse != null && reponse.get("data") instanceof List<?> liste) {
            for (Object element : liste) {
                if (element instanceof Map<?, ?> op && "ACTIVE".equals(texte(op.get("state")))) {
                    operateurs.add(new OperateurPaiement(texte(op.get("operatorKey")), texte(op.get("operatorName"))));
                }
            }
        }
        return operateurs;
    }

    @Override
    public ResultatPaiement initier(DemandePaiement d) {
        Map<String, Object> corps = Map.of(
                "amount", d.montant(),
                "motif", d.motif(),
                "operatorKey", d.operateur(),
                "sourcePhoneNumber", prefixeTelephone + d.telephone(),
                "transactionUniqueID", d.reference());
        try {
            RestClient.RequestBodySpec requete = client.post().uri("/ct-payment/payments/pay/withPhone")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
            if (!vide(d.callbackUrl())) {
                requete = requete.header("callbackUrl", d.callbackUrl());
            }
            return interpreter(requete.body(corps).retrieve().body(JSON));
        } catch (RestClientResponseException e) {
            log.warn("CT Pay a refusé le paiement {} : HTTP {} {}", d.reference(), e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            return ResultatPaiement.echec(messageDe(e.getResponseBodyAsString()));
        } catch (RestClientException e) {
            log.error("CT Pay injoignable pour le paiement {}", d.reference(), e);
            throw new IllegalStateException("Service de paiement injoignable", e);
        }
    }

    @Override
    public ResultatPaiement statut(String processCode) {
        try {
            Map<String, Object> reponse = client.get().uri("/ct-payment/api/processes/{code}", processCode)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JSON);
            ResultatPaiement resultat = interpreter(reponse);
            // Une réponse "ERROR" de l'API elle-même (ex. 404 temporaire) n'est pas un échec du paiement
            return resultat.processCode() == null ? ResultatPaiement.inconnu(processCode, resultat.message()) : resultat;
        } catch (RestClientException e) {
            log.warn("Statut CT Pay indisponible pour {} : {}", processCode, e.getMessage());
            return ResultatPaiement.inconnu(processCode, "Statut indisponible");
        }
    }

    // ------------------------------------------------------------------

    private static ResultatPaiement interpreter(Map<String, Object> reponse) {
        if (reponse == null) {
            return ResultatPaiement.echec("Réponse vide du service de paiement");
        }
        boolean succes = "SUCCESS".equals(texte(reponse.get("status")));
        if (!succes || !(reponse.get("data") instanceof Map<?, ?> data)) {
            return ResultatPaiement.echec(texte(reponse.get("message")));
        }
        return new ResultatPaiement(
                texte(data.get("processCode")),
                convertir(texte(data.get("processStatus"))),
                entier(data.get("processFeesAmount")),
                texte(reponse.get("message")));
    }

    private static EtatPaiementFournisseur convertir(String statut) {
        if (statut == null) return EtatPaiementFournisseur.EN_ATTENTE;
        return switch (statut.trim().toUpperCase()) {
            case "SUCCESS" -> EtatPaiementFournisseur.REUSSI;
            case "FAILED", "ERROR", "CANCELLED" -> EtatPaiementFournisseur.ECHOUE;
            default -> EtatPaiementFournisseur.EN_ATTENTE;
        };
    }

    private static String messageDe(String corps) {
        if (corps != null) {
            Matcher m = MESSAGE.matcher(corps);
            if (m.find()) return m.group(1);
        }
        return "Paiement refusé par l'opérateur";
    }

    private static String texte(Object o) {
        return o == null ? null : o.toString().trim();
    }

    private static Integer entier(Object o) {
        return (o instanceof Number n) ? (int) Math.round(n.doubleValue()) : null;
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
