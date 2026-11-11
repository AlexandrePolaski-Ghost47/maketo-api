package com.maketo.credit.service;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.service.AnnonceChargeur;
import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.credit.dto.AchatResponse;
import com.maketo.credit.dto.CreditManuelRequest;
import com.maketo.credit.dto.OffreResponse;
import com.maketo.credit.dto.PortefeuilleResponse;
import com.maketo.credit.dto.TransactionResponse;
import com.maketo.credit.entity.Offre;
import com.maketo.credit.entity.TransactionCredit;
import com.maketo.credit.entity.TypeOffre;
import com.maketo.credit.entity.TypeTransaction;
import com.maketo.credit.repository.OffreRepository;
import com.maketo.credit.repository.TransactionCreditRepository;
import com.maketo.parametre.entity.ParametreCle;
import com.maketo.parametre.service.ParametreService;
import com.maketo.utilisateur.entity.Utilisateur;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Dépenses de crédits. Le solde est protégé par le verrou optimiste (@Version) de Utilisateur :
 * deux achats simultanés ne peuvent pas dépenser deux fois les mêmes crédits.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortefeuilleServiceImpl implements PortefeuilleService {

    private static final String BOOST_PAR_DEFAUT = "BOOST_24H";
    private static final String PRO_PAR_DEFAUT = "PRO_MENSUEL";

    private final UtilisateurRepository utilisateurRepository;
    private final OffreRepository offreRepository;
    private final TransactionCreditRepository transactionRepository;
    private final AnnonceChargeur annonceChargeur;
    private final ParametreService parametres;
    private final Clock clock;

    @Override
    public List<OffreResponse> offres() {
        return offreRepository.findByActifTrueOrderByTypeAscPrixAsc().stream()
                .map(o -> new OffreResponse(o.getCode(), o.getLibelle(), o.getType(), o.getPrix(), o.getDureeHeures()))
                .toList();
    }

    @Override
    public PortefeuilleResponse portefeuille(Long utilisateurId) {
        return versPortefeuille(charger(utilisateurId));
    }

    @Override
    public Page<TransactionResponse> historique(Long utilisateurId, Pageable pageable) {
        Pageable simple = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return transactionRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId, simple)
                .map(t -> new TransactionResponse(t.getId(), t.getType(), t.getMontant(), t.getSoldeApres(),
                        libelle(t), t.getAnnonce() == null ? null : t.getAnnonce().getId(), t.getDateCreation()));
    }

    @Override
    @Transactional
    public AchatResponse booster(Long annonceId, Long utilisateurId, String offreCode) {
        PaiementsOuverts.verifier(parametres);
        Instant maintenant = clock.instant();
        Annonce annonce = annonceChargeur.chargerPourProprietaire(annonceId, utilisateurId);
        if (!annonce.estVisible(maintenant)) {
            throw new MetierException("Seule une annonce en ligne peut être mise à la une");
        }
        Offre offre = offre(offreCode, BOOST_PAR_DEFAUT, TypeOffre.BOOST);
        Utilisateur utilisateur = charger(utilisateurId);

        utilisateur.debiter(offre.getPrix());
        // Un boost acheté pendant un boost en cours s'ajoute à la suite
        Instant debut = annonce.estBoostee(maintenant) ? annonce.getBoostJusquA() : maintenant;
        annonce.booster(debut.plus(offre.getDuree()));
        transactionRepository.save(TransactionCredit.achat(utilisateur, TypeTransaction.BOOST, offre, annonce));

        return new AchatResponse(utilisateur.getSoldeCredits(), annonce.getBoostJusquA());
    }

    @Override
    @Transactional
    public AchatResponse passerPro(Long utilisateurId, String offreCode) {
        PaiementsOuverts.verifier(parametres);
        Offre offre = offre(offreCode, PRO_PAR_DEFAUT, TypeOffre.ABONNEMENT);
        Utilisateur utilisateur = charger(utilisateurId);

        utilisateur.debiter(offre.getPrix());
        utilisateur.prolongerPro(offre.getDuree(), clock.instant());
        transactionRepository.save(TransactionCredit.achat(utilisateur, TypeTransaction.ABONNEMENT_PRO, offre, null));

        return new AchatResponse(utilisateur.getSoldeCredits(), utilisateur.getProExpireLe());
    }

    @Override
    @Transactional
    public PortefeuilleResponse crediterManuellement(Long utilisateurId, CreditManuelRequest r, Long adminId) {
        if (r.type() != TypeTransaction.BONUS && r.type() != TypeTransaction.REMBOURSEMENT) {
            throw new MetierException("Type autorisé : BONUS ou REMBOURSEMENT");
        }
        Utilisateur utilisateur = charger(utilisateurId);
        utilisateur.crediter(r.montant());
        transactionRepository.save(TransactionCredit.ajustement(utilisateur, r.type(), r.montant(),
                utilisateurRepository.getReferenceById(adminId)));
        return versPortefeuille(utilisateur);
    }

    // ------------------------------------------------------------------

    private Utilisateur charger(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
    }

    private Offre offre(String code, String parDefaut, TypeOffre typeAttendu) {
        String choisi = (code == null || code.isBlank()) ? parDefaut : code.trim().toUpperCase();
        Offre offre = offreRepository.findByCodeAndActifTrue(choisi)
                .orElseThrow(() -> new RessourceIntrouvableException("Offre introuvable"));
        if (offre.getType() != typeAttendu) {
            throw new MetierException("Cette offre ne correspond pas à cet achat");
        }
        return offre;
    }

    private PortefeuilleResponse versPortefeuille(Utilisateur u) {
        Instant maintenant = clock.instant();
        return new PortefeuilleResponse(u.getSoldeCredits(), u.estPro(maintenant), u.getProExpireLe(),
                parametres.booleen(ParametreCle.PAIEMENTS_ACTIFS));
    }

    private static String libelle(TransactionCredit t) {
        if (t.getOffre() != null) {
            return t.getOffre().getLibelle();
        }
        return switch (t.getType()) {
            case RECHARGE -> "Recharge Mobile Money";
            case BONUS -> "Bonus Maketo";
            case REMBOURSEMENT -> "Remboursement";
            default -> t.getType().name();
        };
    }
}
