package com.maketo.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Synchronise des actions hors base de données (fichiers) avec le résultat de la transaction :
 * - un fichier envoyé est supprimé si la transaction échoue (pas de fichier orphelin) ;
 * - un fichier n'est effacé qu'une fois la suppression en base validée (pas de photo cassée).
 */
@Slf4j
public final class ApresTransaction {

    private ApresTransaction() {
    }

    public static void siValidee(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            executer(action);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                executer(action);
            }
        });
    }

    public static void siAnnulee(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    executer(action);
                }
            }
        });
    }

    private static void executer(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            log.warn("Action post-transaction échouée", e);
        }
    }
}
