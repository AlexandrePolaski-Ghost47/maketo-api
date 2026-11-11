package com.maketo.parametre.controller;

import com.maketo.parametre.dto.ParametreRequest;
import com.maketo.parametre.dto.ParametreResponse;
import com.maketo.parametre.service.ParametreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/parametres")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminParametreController {

    private final ParametreService parametreService;

    @GetMapping
    public List<ParametreResponse> lister() {
        return parametreService.lister();
    }

    @PutMapping("/{cle}")
    public ParametreResponse modifier(@PathVariable String cle, @Valid @RequestBody ParametreRequest requete) {
        return parametreService.modifier(cle, requete.valeur());
    }
}
