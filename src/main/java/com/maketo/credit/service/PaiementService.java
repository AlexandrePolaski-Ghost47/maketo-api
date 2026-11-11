package com.maketo.credit.service;

import com.maketo.credit.dto.OptionsRechargeResponse;
import com.maketo.credit.dto.PaiementAdminResponse;
import com.maketo.credit.dto.PaiementResponse;
import com.maketo.credit.dto.RechargeRequest;
import com.maketo.credit.entity.StatutPaiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaiementService {

    OptionsRechargeResponse options();

    PaiementResponse initierRecharge(Long utilisateurId, RechargeRequest requete);

    /** Renvoie l'état de la recharge ; s'il est encore en attente, le revérifie auprès du fournisseur. */
    PaiementResponse suivre(Long utilisateurId, String reference);

    /** Webhook : l'identifiant reçu n'est qu'un signal, le statut est TOUJOURS revérifié chez le fournisseur. */
    void traiterNotification(String identifiant);

    /** Tâche planifiée : revérifie les paiements restés en attente. */
    void reconcilier();

    Page<PaiementAdminResponse> lister(StatutPaiement statut, Pageable pageable);
}
