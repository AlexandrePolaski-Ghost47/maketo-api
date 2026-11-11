package com.maketo.annonce;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.EtatArticle;
import com.maketo.annonce.entity.Formule;
import com.maketo.annonce.entity.PhotoAnnonce;
import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.annonce.entity.TypeAnnonce;
import com.maketo.categorie.entity.Categorie;
import com.maketo.common.exception.MetierException;
import com.maketo.utilisateur.entity.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnnonceTest {

    private static final Instant MAINTENANT = Instant.parse("2027-01-10T12:00:00Z");

    private Annonce annonce() {
        Utilisateur vendeur = Utilisateur.inscrire("N", "H", "697000001", null, "M1", null, null, "hash");
        Categorie categorie = Categorie.creer("Divers", "divers", "box", 1);
        return Annonce.publier(vendeur, categorie, TypeAnnonce.ARTICLE, "Lit", null, 25000, false, EtatArticle.OCCASION, "Campus",
                Formule.GRATUITE, MAINTENANT.plus(Duration.ofDays(7)));
    }

    @Test
    void prixObligatoireSaufADebattre() {
        Utilisateur v = Utilisateur.inscrire("N", "H", "697000001", null, "M1", null, null, "hash");
        Categorie c = Categorie.creer("Divers", "divers", "box", 1);
        assertThatThrownBy(() -> Annonce.publier(v, c, TypeAnnonce.ARTICLE, "Lit", null, null, false, EtatArticle.NEUF, null,
                Formule.GRATUITE, MAINTENANT)).isInstanceOf(MetierException.class);
        Annonce aDebattre = Annonce.publier(v, c, TypeAnnonce.ARTICLE, "Lit", null, null, true, EtatArticle.NEUF, null,
                Formule.GRATUITE, MAINTENANT.plusSeconds(60));
        assertThat(aDebattre.getPrix()).isNull();
    }

    @Test
    void visibleSeulementAvantExpiration() {
        Annonce a = annonce();
        assertThat(a.estVisible(MAINTENANT)).isTrue();
        assertThat(a.estVisible(MAINTENANT.plus(Duration.ofDays(8)))).isFalse();
    }

    @Test
    void uneAnnonceVendueNestPlusModifiable() {
        Annonce a = annonce();
        a.marquerVendue();
        assertThatThrownBy(() -> a.modifier(a.getCategorie(), "X", null, 1, false, EtatArticle.NEUF, null))
                .isInstanceOf(MetierException.class);
    }

    @Test
    void suspensionPuisRetablissement() {
        Annonce a = annonce();
        a.suspendre();
        assertThat(a.getStatut()).isEqualTo(StatutAnnonce.SUSPENDUE);
        assertThat(a.estVisible(MAINTENANT)).isFalse();
        assertThatThrownBy(() -> a.ajouterPhoto("u", "id", 4)).isInstanceOf(MetierException.class);
        a.retablir();
        assertThat(a.estVisible(MAINTENANT)).isTrue();
    }

    @Test
    void limiteDePhotosEtRenumerotationApresSuppression() {
        Annonce a = annonce();
        for (int i = 0; i < 3; i++) {
            PhotoAnnonce p = a.ajouterPhoto("url" + i, "id" + i, 3);
            ReflectionTestUtils.setField(p, "id", (long) i + 1);
        }
        assertThatThrownBy(() -> a.ajouterPhoto("url4", "id4", 3)).isInstanceOf(MetierException.class);

        a.retirerPhoto(1L); // la première
        assertThat(a.getPhotos()).extracting(PhotoAnnonce::getUrl).containsExactly("url1", "url2");
        assertThat(a.getPhotos()).extracting(PhotoAnnonce::getOrdre).containsExactly((short) 0, (short) 1);
    }

    @Test
    void renouvellementRemetEnLigne() {
        Annonce a = annonce();
        a.expirer();
        Instant plusTard = MAINTENANT.plus(Duration.ofDays(9));
        a.renouveler(Formule.GRATUITE, plusTard.plus(Duration.ofDays(7)), plusTard);
        assertThat(a.estVisible(plusTard)).isTrue();
        assertThat(a.getNbRenouvellements()).isEqualTo(1);
    }

    @Test
    void petiteAnnonceSansPrixNiEtatAvecTitreDeduit() {
        Utilisateur v = Utilisateur.inscrire("Amougou", "J", "697000003", null, "M3", null, null, "hash");
        Categorie c = Categorie.creer("Petites annonces", "petites-annonces", "message", 0);
        String message = "Salut, je suis Amougou et je cherche des élèves de Terminale pour des répétitions de maths et de PCT le week-end.";
        Annonce a = Annonce.publier(v, c, TypeAnnonce.MESSAGE, null, message, 5000, true, EtatArticle.NEUF, "Campus",
                Formule.GRATUITE, MAINTENANT.plus(Duration.ofDays(7)));

        assertThat(a.estMessage()).isTrue();
        assertThat(a.getPrix()).isNull();
        assertThat(a.getEtat()).isNull();
        assertThat(a.getPrixADebattre()).isFalse();
        assertThat(a.getTitre()).startsWith("Salut, je suis Amougou").endsWith("…").hasSizeLessThanOrEqualTo(80);
    }

    @Test
    void petiteAnnonceSansMessageRefusee() {
        Utilisateur v = Utilisateur.inscrire("A", "J", "697000003", null, "M3", null, null, "hash");
        Categorie c = Categorie.creer("Petites annonces", "petites-annonces", "message", 0);
        assertThatThrownBy(() -> Annonce.publier(v, c, TypeAnnonce.MESSAGE, null, "  court ", null, false, null, null,
                Formule.GRATUITE, MAINTENANT)).isInstanceOf(MetierException.class);
    }
}
