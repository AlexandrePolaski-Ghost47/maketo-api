package com.maketo.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Point unique de traduction des erreurs en réponses HTTP (format RFC 9457 "problem+json").
 * Aucun détail technique (stacktrace, SQL...) n'est jamais renvoyé au client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Couvre le JSON (MethodArgumentNotValidException, sous-classe) et les formulaires multipart. */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> validation(BindException ex) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erreurs.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ResponseEntity<ProblemDetail> reponse =
                probleme(HttpStatus.BAD_REQUEST, "Données invalides", "Certains champs sont incorrects");
        reponse.getBody().setProperty("erreurs", erreurs);
        return reponse;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> corpsIllisible(HttpMessageNotReadableException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Requête invalide", "Le corps de la requête est mal formé");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> fichierTropLourd(MaxUploadSizeExceededException ex) {
        HttpStatusCode trop = HttpStatusCode.valueOf(413);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(trop, "Photo trop lourde (5 Mo maximum)");
        pd.setTitle("Fichier trop volumineux");
        return ResponseEntity.status(trop).body(pd);
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ProblemDetail> introuvable(RessourceIntrouvableException ex) {
        return probleme(HttpStatus.NOT_FOUND, "Introuvable", ex.getMessage());
    }

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<ProblemDetail> conflit(ConflitException ex) {
        return probleme(HttpStatus.CONFLICT, "Conflit", ex.getMessage());
    }

    @ExceptionHandler(MetierException.class)
    public ResponseEntity<ProblemDetail> metier(MetierException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Action impossible", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> integrite(DataIntegrityViolationException ex) {
        log.warn("Violation de contrainte : {}", ex.getMostSpecificCause().getMessage());
        return probleme(HttpStatus.CONFLICT, "Conflit", "Cette donnée existe déjà ou est invalide");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> concurrence(ObjectOptimisticLockingFailureException ex) {
        return probleme(HttpStatus.CONFLICT, "Conflit", "Opération simultanée détectée, réessaie");
    }

    @ExceptionHandler({IdentifiantsInvalidesException.class, SessionInvalideException.class})
    public ResponseEntity<ProblemDetail> nonAuthentifie(RuntimeException ex) {
        return probleme(HttpStatus.UNAUTHORIZED, "Non authentifié", ex.getMessage());
    }

    @ExceptionHandler(CompteNonActifException.class)
    public ResponseEntity<ProblemDetail> compteNonActif(CompteNonActifException ex) {
        return probleme(HttpStatus.FORBIDDEN, "Compte inactif", ex.getMessage());
    }

    @ExceptionHandler(AccesInterditException.class)
    public ResponseEntity<ProblemDetail> accesInterdit(AccesInterditException ex) {
        return probleme(HttpStatus.FORBIDDEN, "Accès refusé", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> accesRefuse(AccessDeniedException ex) {
        return probleme(HttpStatus.FORBIDDEN, "Accès refusé", "Tu n'as pas les droits pour cette action");
    }

    @ExceptionHandler(TropDeTentativesException.class)
    public ResponseEntity<ProblemDetail> tropDeTentatives(TropDeTentativesException ex) {
        return probleme(HttpStatus.TOO_MANY_REQUESTS, "Trop de tentatives", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> inattendue(Exception ex) {
        // Exceptions Spring MVC standard (404 de route, méthode non supportée, cookie manquant...) : on garde leur statut
        if (ex instanceof ErrorResponse erreurSpring) {
            return ResponseEntity.status(erreurSpring.getStatusCode()).body(erreurSpring.getBody());
        }
        log.error("Erreur inattendue", ex);
        return probleme(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur serveur", "Une erreur est survenue, réessaie plus tard");
    }

    private static ResponseEntity<ProblemDetail> probleme(HttpStatus status, String titre, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(titre);
        return ResponseEntity.status(status).body(pd);
    }
}
