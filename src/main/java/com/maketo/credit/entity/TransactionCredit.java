package com.maketo.credit.entity;

import com.maketo.annonce.entity.Annonce;
import com.maketo.common.entity.BaseEntity;
import com.maketo.utilisateur.entity.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

/**
 * Ligne du journal des crédits. Immuable : une transaction ne se modifie jamais,
 * une erreur se corrige par une nouvelle transaction (ex. REMBOURSEMENT).
 */
@Entity
@Immutable
@Table(name = "transaction_credit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionCredit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeTransaction type;

    /** Positif = entrée de crédits, négatif = dépense. */
    @Column(nullable = false)
    private Integer montant;

    @Column(name = "solde_apres", nullable = false)
    private Integer soldeApres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id")
    private Offre offre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annonce_id")
    private Annonce annonce;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_id", unique = true)
    private Paiement paiement;

    @Column(name = "reference_momo", unique = true, length = 50)
    private String referenceMomo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par")
    private Utilisateur validePar;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    /** Recharge validée par un admin. À appeler APRÈS utilisateur.crediter(montant). */
    public static TransactionCredit recharge(Utilisateur utilisateur, int montant, String referenceMomo,
                                             Utilisateur admin) {
        TransactionCredit t = base(utilisateur, TypeTransaction.RECHARGE, montant);
        t.referenceMomo = referenceMomo;
        t.validePar = admin;
        return t;
    }

    /** Recharge payée en ligne (Mobile Money). À appeler APRÈS utilisateur.crediter(montant). */
    public static TransactionCredit rechargeEnLigne(Utilisateur utilisateur, Paiement paiement) {
        TransactionCredit t = base(utilisateur, TypeTransaction.RECHARGE, paiement.getMontant());
        t.paiement = paiement;
        return t;
    }

    /** Bonus ou remboursement accordé par un admin. À appeler APRÈS utilisateur.crediter(montant). */
    public static TransactionCredit ajustement(Utilisateur utilisateur, TypeTransaction type, int montant,
                                               Utilisateur admin) {
        TransactionCredit t = base(utilisateur, type, montant);
        t.validePar = admin;
        return t;
    }

    /** Achat d'une offre. À appeler APRÈS utilisateur.debiter(prix). */
    public static TransactionCredit achat(Utilisateur utilisateur, TypeTransaction type, Offre offre,
                                          Annonce annonce) {
        TransactionCredit t = base(utilisateur, type, -offre.getPrix());
        t.offre = offre;
        t.annonce = annonce;
        return t;
    }

    private static TransactionCredit base(Utilisateur utilisateur, TypeTransaction type, int montant) {
        TransactionCredit t = new TransactionCredit();
        t.utilisateur = utilisateur;
        t.type = type;
        t.montant = montant;
        t.soldeApres = utilisateur.getSoldeCredits();
        return t;
    }
}
