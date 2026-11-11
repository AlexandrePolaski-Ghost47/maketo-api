package com.maketo.credit.service;

import com.maketo.common.exception.CompteNonActifException;
import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.common.util.TelephoneUtils;
import com.maketo.config.MaketoProperties;
import com.maketo.credit.dto.OptionsRechargeResponse;
import com.maketo.credit.dto.PaiementAdminResponse;
import com.maketo.credit.dto.PaiementResponse;
import com.maketo.credit.dto.RechargeRequest;
import com.maketo.credit.entity.Paiement;
import com.maketo.credit.entity.StatutPaiement;
import com.maketo.credit.entity.TransactionCredit;
import com.maketo.credit.fournisseur.DemandePaiement;
import com.maketo.credit.fournisseur.EtatPaiementFournisseur;
import com.maketo.credit.fournisseur.FournisseurPaiement;
import com.maketo.credit.fournisseur.ResultatPaiement;
import com.maketo.credit.repository.PaiementRepository;
import com.maketo.credit.repository.TransactionCreditRepository;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;
import com.maketo.utilisateur.entity.Utilisateur;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Cycle de vie d'une recharge :
 * 1. enregistrement EN_ATTENTE (transaction courte, validée AVANT l'appel au fournisseur) ;
 * 2. appel au fournisseur HORS transaction (aucune connexion base bloquée pendant l'appel réseau) ;
 * 3. finalisation sous verrou : le portefeuille n'est crédité qu'une fois, quel que soit
 *    le nombre de webhooks / vérifications reçus.
 */
@Slf4j
@Service
public class PaiementServiceImpl implements PaiementService {

    private static final Duration DELAI_EXPIRATION = Duration.ofMinutes(30);
    private static final Duration FENETRE_ANTI_SPAM = Duration.ofMinutes(15);
    private static final int MAX_PAIEMENTS_EN_ATTENTE = 3;
    private static final DateTimeFormatter FORMAT_REFERENCE =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);
    private static final SecureRandom ALEATOIRE = new SecureRandom();

    private final PaiementRepository paiementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final TransactionCreditRepository transactionRepository;
    private final FournisseurPaiement fournisseur;
    private final ParametreService parametres;
    private final MaketoProperties properties;
    private final TransactionTemplate transaction;
    private final Clock clock;

    public PaiementServiceImpl(PaiementRepository paiementRepository, UtilisateurRepository utilisateurRepository,
                               TransactionCreditRepository transactionRepository, FournisseurPaiement fournisseur,
                               ParametreService parametres, MaketoProperties properties,
                               PlatformTransactionManager transactionManager, Clock clock) {
        this.paiementRepository = paiementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.transactionRepository = transactionRepository;
        this.fournisseur = fournisseur;
        this.parametres = parametres;
        this.properties = properties;
        this.transaction = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    // ------------------------------------------------------------------
    // Étudiant
    // ------------------------------------------------------------------

    @Override
    public OptionsRechargeResponse options() {
        boolean actifs = parametres.booleen(ParametreCle.PAIEMENTS_ACTIFS);
        return new OptionsRechargeResponse(actifs, parametres.listeEntiers(ParametreCle.MONTANTS_RECHARGE),
                actifs ? fournisseur.operateurs() : List.of());
    }

    @Override
    public PaiementResponse initierRecharge(Long utilisateurId, RechargeRequest r) {
        PaiementsOuverts.verifier(parametres);
        if (!parametres.listeEntiers(ParametreCle.MONTANTS_RECHARGE).contains(r.montant())) {
            throw new MetierException("Ce montant de recharge n'est pas proposé");
        }
        String telephone = TelephoneUtils.normaliser(r.telephone());
        String operateur = r.operateur().trim().toUpperCase();

        // 1. Enregistrement (validé tout de suite : le webhook doit pouvoir retrouver le paiement)
        Long paiementId = transaction.execute(statut -> enregistrer(utilisateurId, operateur, telephone, r.montant()));
        Paiement paiement = paiementRepository.findById(paiementId).orElseThrow();

        // 2. Appel au fournisseur, hors transaction
        DemandePaiement demande = new DemandePaiement(paiement.getReference(), paiement.getMontant(),
                "Recharge Maketo " + paiement.getMontant() + " F", operateur, telephone, callbackUrl());
        ResultatPaiement resultat;
        try {
            resultat = fournisseur.initier(demande);
        } catch (RuntimeException e) {
            appliquer(paiementId, ResultatPaiement.echec("Service de paiement indisponible"));
            throw new MetierException("Le service de paiement est momentanément indisponible, réessaie plus tard");
        }

        // 3. Mise à jour avec la réponse du fournisseur
        appliquer(paiementId, resultat);
        return reponse(paiementId);
    }

    @Override
    public PaiementResponse suivre(Long utilisateurId, String reference) {
        Long paiementId = transaction.execute(statut -> {
            Paiement p = paiementRepository.findByReference(reference)
                    .orElseThrow(() -> new RessourceIntrouvableException("Paiement introuvable"));
            if (!p.getUtilisateur().getId().equals(utilisateurId)) {
                throw new RessourceIntrouvableException("Paiement introuvable"); // on ne révèle pas son existence
            }
            return p.getId();
        });
        Paiement p = paiementRepository.findById(paiementId).orElseThrow();
        if (p.estEnAttente()) {
            synchroniser(paiementId);
        }
        return reponse(paiementId);
    }

    // ------------------------------------------------------------------
    // Webhook et rattrapage
    // ------------------------------------------------------------------

    @Override
    public void traiterNotification(String identifiant) {
        if (identifiant == null || identifiant.isBlank()) {
            return;
        }
        Optional<Long> paiementId = transaction.execute(statut -> paiementRepository.findByProcessCode(identifiant)
                .or(() -> paiementRepository.findByReference(identifiant))
                .map(Paiement::getId));
        if (paiementId == null || paiementId.isEmpty()) {
            log.warn("Notification de paiement pour un identifiant inconnu : {}", identifiant);
            return;
        }
        synchroniser(paiementId.get());
    }

    @Override
    public void reconcilier() {
        Instant limite = clock.instant().minus(Duration.ofMinutes(1));
        List<Long> ids = transaction.execute(statut -> paiementRepository
                .findByStatutAndDateCreationBeforeOrderByDateCreationAsc(StatutPaiement.EN_ATTENTE, limite,
                        PageRequest.of(0, 50))
                .stream().map(Paiement::getId).toList());
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            try {
                synchroniser(id);
            } catch (RuntimeException e) {
                log.warn("Rattrapage du paiement {} impossible : {}", id, e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    @Override
    public Page<PaiementAdminResponse> lister(StatutPaiement statut, Pageable pageable) {
        Pageable simple = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return transaction.execute(s -> (statut == null
                ? paiementRepository.findAllByOrderByDateCreationDesc(simple)
                : paiementRepository.findByStatutOrderByDateCreationDesc(statut, simple))
                .map(p -> new PaiementAdminResponse(p.getId(), p.getReference(), p.getProcessCode(), p.getFournisseur(),
                        p.getUtilisateur().getId(),
                        p.getUtilisateur().getPrenom() + " " + p.getUtilisateur().getNom(),
                        p.getOperateur(), p.getTelephone(), p.getMontant(), p.getFrais(), p.getStatut(),
                        p.getMessageErreur(), p.getDateCreation(), p.getDateFinalisation())));
    }

    // ------------------------------------------------------------------
    // Mécanique interne
    // ------------------------------------------------------------------

    private Long enregistrer(Long utilisateurId, String operateur, String telephone, int montant) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
        if (!utilisateur.estActif()) {
            throw new CompteNonActifException("Ton compte n'est pas actif");
        }
        long enAttente = paiementRepository.countByUtilisateurIdAndStatutAndDateCreationAfter(
                utilisateurId, StatutPaiement.EN_ATTENTE, clock.instant().minus(FENETRE_ANTI_SPAM));
        if (enAttente >= MAX_PAIEMENTS_EN_ATTENTE) {
            throw new MetierException("Tu as déjà des paiements en attente : valide-les sur ton téléphone "
                    + "ou réessaie dans quelques minutes");
        }
        Paiement paiement = Paiement.creer(utilisateur, nouvelleReference(), fournisseur.code(), operateur,
                telephone, montant);
        return paiementRepository.save(paiement).getId();
    }

    /** Interroge le fournisseur (hors transaction) puis applique le résultat. */
    private void synchroniser(Long paiementId) {
        String processCode = transaction.execute(statut -> paiementRepository.findById(paiementId)
                .map(Paiement::getProcessCode).orElse(null));
        ResultatPaiement resultat = (processCode == null)
                ? ResultatPaiement.inconnu(null, "Paiement non transmis au fournisseur")
                : fournisseur.statut(processCode);
        appliquer(paiementId, resultat);
    }

    /** Seul endroit où un paiement change d'état et où le portefeuille est crédité. */
    private void appliquer(Long paiementId, ResultatPaiement resultat) {
        transaction.executeWithoutResult(statut -> {
            Paiement paiement = paiementRepository.verrouiller(paiementId).orElseThrow();
            Instant maintenant = clock.instant();
            if (paiement.getProcessCode() == null && resultat.processCode() != null) {
                paiement.associerProcessCode(resultat.processCode());
            }

            switch (resultat.etat()) {
                case REUSSI -> crediter(paiement, resultat.frais(), maintenant);
                case ECHOUE -> {
                    if (paiement.estEnAttente()) {
                        paiement.marquerEchoue(resultat.message(), maintenant);
                    }
                }
                case EN_ATTENTE -> {
                    if (paiement.estEnAttente() && paiement.getDateCreation().plus(DELAI_EXPIRATION).isBefore(maintenant)) {
                        paiement.marquerEchoue("Délai de validation dépassé", maintenant);
                    }
                }
            }
        });
    }

    /**
     * Idempotent : déjà REUSSI => rien. Un paiement marqué échoué par expiration mais confirmé ensuite
     * par le fournisseur est bien crédité (le fournisseur fait foi).
     */
    private void crediter(Paiement paiement, Integer frais, Instant maintenant) {
        if (paiement.estReussi()) {
            return;
        }
        paiement.marquerReussi(frais, maintenant);
        Utilisateur utilisateur = paiement.getUtilisateur();
        utilisateur.crediter(paiement.getMontant());
        transactionRepository.save(TransactionCredit.rechargeEnLigne(utilisateur, paiement));
        log.info("Recharge {} réussie : +{} F pour l'utilisateur {}", paiement.getReference(),
                paiement.getMontant(), utilisateur.getId());
    }

    private PaiementResponse reponse(Long paiementId) {
        Paiement p = paiementRepository.findById(paiementId).orElseThrow();
        String message = switch (p.getStatut()) {
            case EN_ATTENTE -> "Valide le paiement sur ton téléphone (code PIN Mobile Money)";
            case REUSSI -> "Recharge réussie : " + p.getMontant() + " F ajoutés à ton portefeuille";
            case ECHOUE -> p.getMessageErreur() != null ? p.getMessageErreur() : "Le paiement a échoué";
        };
        return new PaiementResponse(p.getReference(), p.getMontant(), p.getOperateur(), p.getStatut(), message,
                p.getDateCreation());
    }

    private String nouvelleReference() {
        byte[] octets = new byte[4];
        ALEATOIRE.nextBytes(octets);
        return "MKT-" + FORMAT_REFERENCE.format(clock.instant()) + "-" + HexFormat.of().formatHex(octets);
    }

    private String callbackUrl() {
        MaketoProperties.Paiement config = properties.paiement();
        return (config == null || config.callbackUrl() == null || config.callbackUrl().isBlank())
                ? null : config.callbackUrl();
    }
}
