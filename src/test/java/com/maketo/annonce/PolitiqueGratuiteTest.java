package com.maketo.annonce;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.Formule;
import com.maketo.annonce.policy.PolitiqueGratuite;
import com.maketo.annonce.repository.AnnonceRepository;
import com.maketo.common.exception.MetierException;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;
import com.maketo.utilisateur.entity.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolitiqueGratuiteTest {

    private static final Instant MI_JANVIER = Instant.parse("2027-01-15T10:00:00Z");

    @Mock
    private ParametreService parametres;
    @Mock
    private AnnonceRepository annonceRepository;

    private PolitiqueGratuite politique;
    private Utilisateur vendeur;

    @BeforeEach
    void preparer() {
        politique = new PolitiqueGratuite(parametres, annonceRepository);
        vendeur = Utilisateur.inscrire("N", "H", "697000001", null, "M1", null, null, "hash");
        ReflectionTestUtils.setField(vendeur, "id", 2L);
    }

    @Test
    void pasDeQuotaPendantLeLancement() {
        when(parametres.date(ParametreCle.DATE_FIN_PERIODE_GRATUITE)).thenReturn(LocalDate.of(2027, 1, 31));
        assertThatCode(() -> politique.verifierDroit(vendeur, null, MI_JANVIER)).doesNotThrowAnyException();
        verify(annonceRepository, never()).compterPublicationsGratuitesDepuis(any(), any());
    }

    @Test
    void refuseQuandLeQuotaEstAtteint() {
        apresLancement();
        when(parametres.entier(ParametreCle.QUOTA_GRATUIT_MENSUEL)).thenReturn(3);
        when(annonceRepository.compterPublicationsGratuitesDepuis(eq(2L), any())).thenReturn(3L);
        assertThatThrownBy(() -> politique.verifierDroit(vendeur, null, MI_JANVIER))
                .isInstanceOf(MetierException.class)
                .hasMessageContaining("3 publications gratuites");
    }

    @Test
    void autoriseSousLeQuota() {
        apresLancement();
        when(parametres.entier(ParametreCle.QUOTA_GRATUIT_MENSUEL)).thenReturn(3);
        when(annonceRepository.compterPublicationsGratuitesDepuis(eq(2L), any())).thenReturn(2L);
        assertThatCode(() -> politique.verifierDroit(vendeur, null, MI_JANVIER)).doesNotThrowAnyException();
    }

    @Test
    void renouvelerUneAnnonceDejaCompteeCeMoisNeCoutePasDePublication() {
        apresLancement();
        Annonce deja = mock(Annonce.class);
        when(deja.getFormule()).thenReturn(Formule.GRATUITE);
        when(deja.getDateCreation()).thenReturn(Instant.parse("2027-01-03T08:00:00Z"));
        assertThatCode(() -> politique.verifierDroit(vendeur, deja, MI_JANVIER)).doesNotThrowAnyException();
        verify(annonceRepository, never()).compterPublicationsGratuitesDepuis(any(), any());
    }

    private void apresLancement() {
        when(parametres.date(ParametreCle.DATE_FIN_PERIODE_GRATUITE)).thenReturn(LocalDate.of(2026, 12, 31));
    }
}
