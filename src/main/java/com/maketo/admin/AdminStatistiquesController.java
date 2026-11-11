package com.maketo.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatistiquesController {

    private final StatistiquesService statistiquesService;

    @GetMapping("/api/admin/statistiques")
    public StatistiquesResponse statistiques() {
        return statistiquesService.calculer();
    }
}
