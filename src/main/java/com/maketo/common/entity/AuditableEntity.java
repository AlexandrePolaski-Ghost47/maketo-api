package com.maketo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entité avec dates de création et de modification gérées automatiquement.
 */
@Getter
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;
}
