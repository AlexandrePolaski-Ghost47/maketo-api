package com.maketo.credit;

import com.maketo.config.MaketoProperties;
import com.maketo.credit.entity.Paiement;
import com.maketo.credit.entity.StatutPaiement;
import com.maketo.credit.entity.TransactionCredit;
import com.maketo.credit.fournisseur.EtatPaiementFournisseur;
import com.maketo.credit.fournisseur.FournisseurPaiement;
import com.maketo.credit.fournisseur.ResultatPaiement;
import com.maketo.credit.repository.PaiementRepository;
import com.maketo.credit.repository.TransactionCreditRepository;
import com.maketo.credit.service.PaiementServiceImpl;
import com.maketo.parametre.service.ParametreService;
import com.maketo.utilisateur.entity.Utilisateur;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** La règle la plus importante de l'appli : un paiement ne crédite le portefeuille qu'une seule fois. */
@ExtendWith(MockitoExtension.class)
class PaiementServiceImplTest {

    @Mock private PaiementRepository paiementRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private TransactionCreditRepository transactionRepository;
    @Mock private FournisseurPaiement fournisseur;
    @Mock private ParametreService parametres;
    @Mock private MaketoProperties properties;
    @Mock private PlatformTransactionManager transactionManager;

    private PaiementServiceImpl service;
    private Utilisateur etudiant;
    private Paiement paiement;

    @BeforeEach
    void preparer() {
        Clock clock = Clock.fixed(Instant.parse("2027-01-15T10:00:00Z"), ZoneOffset.UTC);
        service = new PaiementServiceImpl(paiementRepository, utilisateurRepository, transactionRepository,
                fournisseur, parametres, properties, transactionManager, clock);

        etudiant = Utilisateur.inscrire("N", "H", "697000001", null, "M1", null, null, "hash");
        paiement = Paiement.creer(etudiant, "MKT-1", "CTPAY", "MOMO", "697000001", 1000);
        ReflectionTestUtils.setField(paiement, "id", 10L);
        paiement.associerProcessCode("PROC_1");

        when(paiementRepository.findByProcessCode("PROC_1")).thenReturn(Optional.of(paiement));
        when(paiementRepository.findById(10L)).thenReturn(Optional.of(paiement));
        when(paiementRepository.verrouiller(10L)).thenReturn(Optional.of(paiement));
    }

    @Test
    void deuxNotificationsDeSuccesNeCreditentQuUneFois() {
        when(fournisseur.statut("PROC_1"))
                .thenReturn(new ResultatPaiement("PROC_1", EtatPaiementFournisseur.REUSSI, 30, "ok"));

        service.traiterNotification("PROC_1");
        service.traiterNotification("PROC_1"); // webhook rejoué / vérification concurrente

        assertThat(etudiant.getSoldeCredits()).isEqualTo(1000);
        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.REUSSI);
        assertThat(paiement.getFrais()).isEqualTo(30);
        verify(transactionRepository, times(1)).save(any(TransactionCredit.class));
    }

    @Test
    void echecChezLeFournisseurNeCreditePas() {
        when(fournisseur.statut("PROC_1"))
                .thenReturn(new ResultatPaiement("PROC_1", EtatPaiementFournisseur.ECHOUE, null, "Solde insuffisant"));

        service.traiterNotification("PROC_1");

        assertThat(etudiant.getSoldeCredits()).isZero();
        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.ECHOUE);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void unPaiementReussiNeRedevientJamaisEchoue() {
        when(fournisseur.statut("PROC_1"))
                .thenReturn(new ResultatPaiement("PROC_1", EtatPaiementFournisseur.REUSSI, 30, "ok"))
                .thenReturn(new ResultatPaiement("PROC_1", EtatPaiementFournisseur.ECHOUE, null, "erreur"));

        service.traiterNotification("PROC_1");
        service.traiterNotification("PROC_1");

        assertThat(paiement.getStatut()).isEqualTo(StatutPaiement.REUSSI);
        assertThat(etudiant.getSoldeCredits()).isEqualTo(1000);
    }
}
