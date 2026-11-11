package com.maketo.credit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Traite le webhook en arrière-plan : on répond 200 tout de suite au fournisseur (exigence CT Pay). */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPaiementHandler {

    private final PaiementService paiementService;

    @Async
    public void traiter(String identifiant) {
        try {
            paiementService.traiterNotification(identifiant);
        } catch (RuntimeException e) {
            log.warn("Traitement de la notification {} impossible (le rattrapage s'en chargera)", identifiant, e);
        }
    }
}
