package com.maketo.credit.entity;

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

import java.time.Instant;

/** Une recharge Mobile Money du portefeuille de crédits. */
@Entity
@Table(name = "paiement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Paiement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "process_code", unique = true, length = 100)
    private String processCode;

    @Column(nullable = false, length = 20)
    private String fournisseur;

    @Column(nullable = false, length = 20)
    private String operateur;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(nullable = false)
    private Integer montant;

    @Column
    private Integer frais;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;

    @Column(name = "message_erreur")
    private String messageErreur;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @Column(name = "date_finalisation")
    private Instant dateFinalisation;

    public static Paiement creer(Utilisateur utilisateur, String reference, String fournisseur, String operateur,
                                 String telephone, int montant) {
        Paiement p = new Paiement();
        p.utilisateur = utilisateur;
        p.reference = reference;
        p.fournisseur = fournisseur;
        p.operateur = operateur;
        p.telephone = telephone;
        p.montant = montant;
        return p;
    }

    public void associerProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public boolean estReussi() {
        return statut == StatutPaiement.REUSSI;
    }

    public boolean estEnAttente() {
        return statut == StatutPaiement.EN_ATTENTE;
    }

    public void marquerReussi(Integer frais, Instant maintenant) {
        this.statut = StatutPaiement.REUSSI;
        this.frais = frais;
        this.messageErreur = null;
        this.dateFinalisation = maintenant;
    }

    /** Un paiement réussi ne redevient jamais échoué. */
    public void marquerEchoue(String message, Instant maintenant) {
        if (statut == StatutPaiement.REUSSI) {
            return;
        }
        this.statut = StatutPaiement.ECHOUE;
        this.messageErreur = (message != null && message.length() > 255) ? message.substring(0, 255) : message;
        this.dateFinalisation = maintenant;
    }
}
