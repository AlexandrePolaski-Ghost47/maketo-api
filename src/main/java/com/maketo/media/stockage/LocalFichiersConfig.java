package com.maketo.media.stockage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/** En stockage local, le serveur sert lui-même les photos sur /fichiers/**. */
@Configuration
@ConditionalOnProperty(prefix = "maketo.stockage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFichiersConfig implements WebMvcConfigurer {

    private final LocalStockageService stockage;

    public LocalFichiersConfig(LocalStockageService stockage) {
        this.stockage = stockage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String emplacement = stockage.dossier().toUri().toString();
        registry.addResourceHandler(LocalStockageService.PREFIXE_URL + "**")
                .addResourceLocations(emplacement.endsWith("/") ? emplacement : emplacement + "/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
    }
}
