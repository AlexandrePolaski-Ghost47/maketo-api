package com.maketo.credit.controller;

import com.maketo.credit.dto.OptionsRechargeResponse;
import com.maketo.credit.dto.PaiementResponse;
import com.maketo.credit.dto.RechargeRequest;
import com.maketo.credit.service.NotificationPaiementHandler;
import com.maketo.credit.service.PaiementService;
import com.maketo.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private static final Pattern ID_JSON = Pattern.compile("\"id\"\\s*:\\s*\"?([A-Za-z0-9_\\-]+)\"?");
    private static final int TAILLE_MAX_CORPS = 10_000;

    private final PaiementService paiementService;
    private final NotificationPaiementHandler notificationHandler;

    @GetMapping("/options")
    public OptionsRechargeResponse options() {
        return paiementService.options();
    }

    @PostMapping("/recharges")
    public ResponseEntity<PaiementResponse> recharger(@Valid @RequestBody RechargeRequest requete,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paiementService.initierRecharge(SecurityUtils.idUtilisateur(jwt), requete));
    }

    @GetMapping("/recharges/{reference}")
    public PaiementResponse suivre(@PathVariable String reference, @AuthenticationPrincipal Jwt jwt) {
        return paiementService.suivre(SecurityUtils.idUtilisateur(jwt), reference);
    }

    /**
     * Webhook CT Pay (public). L'identifiant est lu en paramètre (query/formulaire) ou dans un corps JSON.
     * Le "status" envoyé est IGNORÉ volontairement : il n'est pas signé, on revérifie toujours chez CT Pay.
     * On répond 200 immédiatement, le traitement se fait en arrière-plan.
     */
    @PostMapping("/callback/ctpay")
    public ResponseEntity<Void> callback(HttpServletRequest requete) throws IOException {
        String id = requete.getParameter("id");
        if (id == null) {
            byte[] corps = requete.getInputStream().readNBytes(TAILLE_MAX_CORPS);
            Matcher m = ID_JSON.matcher(new String(corps, StandardCharsets.UTF_8));
            id = m.find() ? m.group(1) : null;
        }
        notificationHandler.traiter(id);
        return ResponseEntity.ok().build();
    }
}
