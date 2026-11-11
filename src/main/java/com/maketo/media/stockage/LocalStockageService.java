package com.maketo.media.stockage;

import com.maketo.config.MaketoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/** Stockage sur le disque du serveur : pour le développement (aucun compte à créer). */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "maketo.stockage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalStockageService implements StockageService {

    public static final String PREFIXE_URL = "/fichiers/";

    private final Path dossier;
    private final String urlBase;

    public LocalStockageService(MaketoProperties properties) {
        MaketoProperties.Stockage.Local config = properties.stockage().local();
        this.dossier = Path.of(config.dossier()).toAbsolutePath().normalize();
        this.urlBase = config.urlBase();
        try {
            Files.createDirectories(dossier);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de créer le dossier " + dossier, e);
        }
    }

    @Override
    public FichierStocke stocker(byte[] contenuJpeg) {
        String nom = UUID.randomUUID() + ".jpg"; // nom généré par nous : jamais celui envoyé par le client
        try {
            Files.write(dossier.resolve(nom), contenuJpeg, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("Écriture de la photo impossible", e);
        }
        return new FichierStocke(urlBase + PREFIXE_URL + nom, nom);
    }

    @Override
    public void supprimer(String identifiant) {
        Path fichier = dossier.resolve(identifiant).normalize();
        if (!fichier.startsWith(dossier)) {
            log.warn("Suppression refusée hors du dossier de stockage : {}", identifiant);
            return;
        }
        try {
            Files.deleteIfExists(fichier);
        } catch (IOException e) {
            log.warn("Suppression de {} impossible", fichier, e);
        }
    }

    public Path dossier() {
        return dossier;
    }
}
