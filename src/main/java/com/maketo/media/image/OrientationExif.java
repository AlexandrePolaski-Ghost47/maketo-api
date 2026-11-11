package com.maketo.media.image;

/**
 * Lit le tag EXIF "Orientation" d'un JPEG (photos de téléphone prises en portrait).
 * Lecture minimale et défensive : en cas de doute, renvoie 1 (= pas de rotation).
 */
final class OrientationExif {

    private OrientationExif() {
    }

    static int lire(byte[] jpeg) {
        try {
            int i = 2; // après le marqueur SOI (FF D8)
            while (i + 4 <= jpeg.length) {
                if ((jpeg[i] & 0xFF) != 0xFF) return 1;
                int marqueur = jpeg[i + 1] & 0xFF;
                if (marqueur == 0xD9 || marqueur == 0xDA) return 1; // fin d'image ou début des données
                int longueur = u16(jpeg, i + 2, false);
                if (marqueur == 0xE1 && longueur >= 8 && estEnteteExif(jpeg, i + 4)) {
                    return lireTiff(jpeg, i + 10, Math.min(jpeg.length, i + 2 + longueur));
                }
                i += 2 + longueur;
            }
        } catch (RuntimeException e) {
            // EXIF mal formé : on ignore
        }
        return 1;
    }

    private static boolean estEnteteExif(byte[] b, int i) {
        return i + 6 <= b.length && b[i] == 'E' && b[i + 1] == 'x' && b[i + 2] == 'i' && b[i + 3] == 'f'
                && b[i + 4] == 0 && b[i + 5] == 0;
    }

    private static int lireTiff(byte[] b, int debut, int fin) {
        if (debut + 8 > fin) return 1;
        boolean littleEndian = b[debut] == 'I' && b[debut + 1] == 'I';
        int ifd = debut + (int) u32(b, debut + 4, littleEndian);
        if (ifd + 2 > fin) return 1;
        int nbEntrees = u16(b, ifd, littleEndian);
        for (int k = 0; k < nbEntrees; k++) {
            int e = ifd + 2 + k * 12;
            if (e + 12 > fin) break;
            if (u16(b, e, littleEndian) == 0x0112) {
                int valeur = u16(b, e + 8, littleEndian);
                return (valeur >= 1 && valeur <= 8) ? valeur : 1;
            }
        }
        return 1;
    }

    private static int u16(byte[] b, int i, boolean le) {
        int a = b[i] & 0xFF, c = b[i + 1] & 0xFF;
        return le ? (c << 8) | a : (a << 8) | c;
    }

    private static long u32(byte[] b, int i, boolean le) {
        long r = 0;
        for (int k = 0; k < 4; k++) {
            int octet = b[i + (le ? 3 - k : k)] & 0xFF;
            r = (r << 8) | octet;
        }
        return r;
    }
}
