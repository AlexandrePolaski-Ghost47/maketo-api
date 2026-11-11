package com.maketo.media.stockage;

import com.maketo.config.MaketoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * Stockage Cloudinary (production) via son API REST signée : aucun SDK externe à maintenir.
 * Les images sont servies par le CDN de Cloudinary au format le plus léger pour chaque téléphone (f_auto, q_auto).
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "maketo.stockage", name = "type", havingValue = "cloudinary")
public class CloudinaryStockageService implements StockageService {

    private static final ParameterizedTypeReference<Map<String, Object>> REPONSE_JSON =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final String apiKey;
    private final String apiSecret;
    private final String dossier;
    private final Clock clock;

    public CloudinaryStockageService(MaketoProperties properties, Clock clock) {
        MaketoProperties.Stockage.Cloudinary config = properties.stockage().cloudinary();
        if (config == null || vide(config.cloudName()) || vide(config.apiKey()) || vide(config.apiSecret())) {
            throw new IllegalStateException("Configuration Cloudinary incomplète (maketo.stockage.cloudinary.*)");
        }
        this.restClient = RestClient.create("https://api.cloudinary.com/v1_1/" + config.cloudName());
        this.apiKey = config.apiKey();
        this.apiSecret = config.apiSecret();
        this.dossier = vide(config.dossier()) ? "maketo" : config.dossier();
        this.clock = clock;
    }

    @Override
    public FichierStocke stocker(byte[] contenuJpeg) {
        String publicId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(clock.instant().getEpochSecond());
        // Paramètres signés, triés par ordre alphabétique (exigence Cloudinary)
        String signature = sha1("folder=" + dossier + "&public_id=" + publicId + "&timestamp=" + timestamp + apiSecret);

        MultiValueMap<String, Object> formulaire = new LinkedMultiValueMap<>();
        formulaire.add("file", new ByteArrayResource(contenuJpeg) {
            @Override
            public String getFilename() {
                return publicId + ".jpg";
            }
        });
        formulaire.add("api_key", apiKey);
        formulaire.add("timestamp", timestamp);
        formulaire.add("folder", dossier);
        formulaire.add("public_id", publicId);
        formulaire.add("signature", signature);

        try {
            Map<String, Object> reponse = restClient.post().uri("/image/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(formulaire)
                    .retrieve()
                    .body(REPONSE_JSON);
            String url = String.valueOf(reponse.get("secure_url")).replace("/upload/", "/upload/f_auto,q_auto/");
            return new FichierStocke(url, String.valueOf(reponse.get("public_id")));
        } catch (RestClientException e) {
            throw new IllegalStateException("Envoi de la photo vers Cloudinary impossible", e);
        }
    }

    @Override
    public void supprimer(String identifiant) {
        String timestamp = String.valueOf(clock.instant().getEpochSecond());
        MultiValueMap<String, Object> formulaire = new LinkedMultiValueMap<>();
        formulaire.add("public_id", identifiant);
        formulaire.add("api_key", apiKey);
        formulaire.add("timestamp", timestamp);
        formulaire.add("signature", sha1("public_id=" + identifiant + "&timestamp=" + timestamp + apiSecret));
        try {
            restClient.post().uri("/image/destroy")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(formulaire)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Suppression Cloudinary de {} impossible", identifiant, e);
        }
    }

    private static String sha1(String texte) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(texte.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
