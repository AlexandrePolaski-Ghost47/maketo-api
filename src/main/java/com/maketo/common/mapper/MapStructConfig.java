package com.maketo.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Configuration commune des mappers : beans Spring, et erreur de compilation
 * si un champ d'un DTO n'est pas rempli (on ne l'oublie jamais en silence).
 */
@MapperConfig(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MapStructConfig {
}
