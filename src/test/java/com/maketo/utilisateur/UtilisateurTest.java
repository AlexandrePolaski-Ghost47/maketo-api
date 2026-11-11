package com.maketo.utilisateur;

import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.SoldeInsuffisantException;
import com.maketo.utilisateur.entity.Utilisateur;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtilisateurTest {

    private Utilisateur etudiant() {
        return Utilisateur.inscrire("Njampou", "Hermann", "697000001", null, "23GSI001", "GSI", "4", "hash");
    }

    @Test
    void whatsappParDefautEgalAuTelephone() {
        assertThat(etudiant().getWhatsapp()).isEqualTo("697000001");
    }

    @Test
    void debitRefuseSiSoldeInsuffisant() {
        Utilisateur u = etudiant();
        u.crediter(50);
        assertThatThrownBy(() -> u.debiter(100)).isInstanceOf(SoldeInsuffisantException.class);
        assertThat(u.getSoldeCredits()).isEqualTo(50);
    }

    @Test
    void montantsNegatifsRefuses() {
        Utilisateur u = etudiant();
        assertThatThrownBy(() -> u.crediter(-10)).isInstanceOf(MetierException.class);
        assertThatThrownBy(() -> u.debiter(0)).isInstanceOf(MetierException.class);
    }

    @Test
    void abonnementProSeCumule() {
        Utilisateur u = etudiant();
        Instant t0 = Instant.parse("2027-01-01T00:00:00Z");
        u.prolongerPro(Duration.ofDays(30), t0);
        u.prolongerPro(Duration.ofDays(30), t0.plus(Duration.ofDays(10))); // racheté avant la fin
        assertThat(u.getProExpireLe()).isEqualTo(t0.plus(Duration.ofDays(60)));
        assertThat(u.estPro(t0.plus(Duration.ofDays(59)))).isTrue();
        assertThat(u.estPro(t0.plus(Duration.ofDays(61)))).isFalse();
    }
}
