package com.maketo.media.image;

import com.maketo.common.exception.MetierException;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Prépare une photo avant stockage :
 * 1. vérifie le VRAI format (signature binaire, pas l'extension ni l'en-tête envoyé par le client) ;
 * 2. refuse les images géantes (protection contre les "bombes" de décompression) ;
 * 3. redresse les photos de téléphone (orientation EXIF) ;
 * 4. redimensionne (1280 px max) et ré-encode en JPEG : fichier léger pour les forfaits data des étudiants,
 *    et toutes les métadonnées (dont la position GPS) sont supprimées.
 */
@Component
public class TraitementImage {

    public static final int COTE_MAX = 1280;
    private static final int DIMENSION_MAX = 10_000;
    private static final long PIXELS_MAX = 50_000_000L;
    private static final float QUALITE_JPEG = 0.82f;

    public byte[] preparer(byte[] original) {
        Format format = detecterFormat(original);
        BufferedImage image = versRgb(lire(original));
        if (format == Format.JPEG) {
            image = appliquerOrientation(image, OrientationExif.lire(original));
        }
        return encoderJpeg(redimensionner(image, COTE_MAX));
    }

    // ------------------------------------------------------------------

    private enum Format { JPEG, PNG }

    private static Format detecterFormat(byte[] b) {
        if (b == null || b.length < 8) {
            throw new MetierException("Fichier vide ou trop petit");
        }
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return Format.JPEG;
        }
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return Format.PNG;
        }
        throw new MetierException("Format non supporté : envoie une photo JPG ou PNG");
    }

    private static BufferedImage lire(byte[] octets) {
        try (ImageInputStream entree = ImageIO.createImageInputStream(new ByteArrayInputStream(octets))) {
            Iterator<ImageReader> lecteurs = ImageIO.getImageReaders(entree);
            if (!lecteurs.hasNext()) {
                throw new MetierException("Image illisible");
            }
            ImageReader lecteur = lecteurs.next();
            try {
                lecteur.setInput(entree, true, true);
                int largeur = lecteur.getWidth(0);
                int hauteur = lecteur.getHeight(0);
                // On lit les dimensions AVANT de décoder : une image de 50 000 x 50 000 ne sera jamais chargée en mémoire
                if (largeur > DIMENSION_MAX || hauteur > DIMENSION_MAX || (long) largeur * hauteur > PIXELS_MAX) {
                    throw new MetierException("Image trop grande");
                }
                BufferedImage image = lecteur.read(0);
                if (image == null) {
                    throw new MetierException("Image illisible");
                }
                return image;
            } finally {
                lecteur.dispose();
            }
        } catch (IOException e) {
            throw new MetierException("Image illisible ou corrompue");
        }
    }

    /** Supprime la transparence (fond blanc) : le JPEG ne la gère pas. */
    private static BufferedImage versRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return rgb;
    }

    static BufferedImage appliquerOrientation(BufferedImage img, int orientation) {
        if (orientation <= 1 || orientation > 8) {
            return img;
        }
        int w = img.getWidth();
        int h = img.getHeight();
        boolean pivote = orientation >= 5;
        AffineTransform t = new AffineTransform();
        switch (orientation) {
            case 2 -> { t.scale(-1, 1); t.translate(-w, 0); }              // miroir horizontal
            case 3 -> { t.translate(w, h); t.rotate(Math.PI); }            // 180°
            case 4 -> { t.scale(1, -1); t.translate(0, -h); }              // miroir vertical
            case 5 -> { t.rotate(-Math.PI / 2); t.scale(-1, 1); }          // transposition
            case 6 -> { t.translate(h, 0); t.rotate(Math.PI / 2); }        // 90° horaire
            case 7 -> { t.scale(-1, 1); t.translate(-h, 0); t.translate(0, w); t.rotate(3 * Math.PI / 2); }
            case 8 -> { t.translate(0, w); t.rotate(3 * Math.PI / 2); }    // 90° anti-horaire
            default -> { return img; }
        }
        BufferedImage resultat = new BufferedImage(pivote ? h : w, pivote ? w : h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultat.createGraphics();
        g.drawImage(img, t, null);
        g.dispose();
        return resultat;
    }

    /** Réduction progressive (par moitiés) : bien plus nette qu'une seule réduction brutale. */
    static BufferedImage redimensionner(BufferedImage img, int coteMax) {
        int w = img.getWidth();
        int h = img.getHeight();
        int plusGrand = Math.max(w, h);
        if (plusGrand <= coteMax) {
            return img;
        }
        double ratio = (double) coteMax / plusGrand;
        int cibleW = Math.max(1, (int) Math.round(w * ratio));
        int cibleH = Math.max(1, (int) Math.round(h * ratio));

        BufferedImage courante = img;
        while (courante.getWidth() / 2 >= cibleW && courante.getHeight() / 2 >= cibleH) {
            courante = dessiner(courante, courante.getWidth() / 2, courante.getHeight() / 2);
        }
        return (courante.getWidth() == cibleW && courante.getHeight() == cibleH)
                ? courante : dessiner(courante, cibleW, cibleH);
    }

    private static BufferedImage dessiner(BufferedImage source, int w, int h) {
        BufferedImage cible = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = cible.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, w, h, null);
        g.dispose();
        return cible;
    }

    private static byte[] encoderJpeg(BufferedImage img) {
        ImageWriter ecrivain = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream octets = new ByteArrayOutputStream();
        try (ImageOutputStream sortie = ImageIO.createImageOutputStream(octets)) {
            ImageWriteParam param = ecrivain.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(QUALITE_JPEG);
            ecrivain.setOutput(sortie);
            ecrivain.write(null, new IIOImage(img, null, null), param);
        } catch (IOException e) {
            throw new IllegalStateException("Encodage JPEG impossible", e);
        } finally {
            ecrivain.dispose();
        }
        return octets.toByteArray();
    }
}
