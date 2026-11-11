package com.maketo.media;

import com.maketo.common.exception.MetierException;
import com.maketo.media.image.TraitementImage;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraitementImageTest {

    private final TraitementImage traitement = new TraitementImage();

    @Test
    void redimensionneLesGrandesPhotos() throws IOException {
        byte[] sortie = traitement.preparer(encoder(new BufferedImage(4000, 3000, BufferedImage.TYPE_INT_RGB), "jpg"));
        BufferedImage resultat = ImageIO.read(new ByteArrayInputStream(sortie));
        assertThat(resultat.getWidth()).isEqualTo(TraitementImage.COTE_MAX);
        assertThat(resultat.getHeight()).isEqualTo(960);
    }

    @Test
    void nAgranditPasLesPetitesImages() throws IOException {
        byte[] sortie = traitement.preparer(encoder(new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB), "png"));
        assertThat(ImageIO.read(new ByteArrayInputStream(sortie)).getWidth()).isEqualTo(300);
    }

    @Test
    void refuseUnFauxFichierEtLesGif() throws IOException {
        assertThatThrownBy(() -> traitement.preparer("<?php echo 1; ?> padding".getBytes()))
                .isInstanceOf(MetierException.class);
        byte[] gif = encoder(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "gif");
        assertThatThrownBy(() -> traitement.preparer(gif)).isInstanceOf(MetierException.class);
    }

    private static byte[] encoder(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        ImageIO.write(image, format, sortie);
        return sortie.toByteArray();
    }
}
