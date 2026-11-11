package com.maketo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Horloge injectable : les services n'appellent jamais Instant.now() directement,
 * ce qui permet de tester les règles de dates (expiration, boost, Pro).
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
