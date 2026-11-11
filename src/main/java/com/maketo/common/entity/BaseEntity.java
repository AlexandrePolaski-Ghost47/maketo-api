package com.maketo.common.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Classe mère de toutes les entités ayant un identifiant technique auto-généré.
 * equals/hashCode basés sur l'id, compatibles avec les proxys Hibernate.
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (classeReelle(this) != classeReelle(o)) return false;
        BaseEntity autre = (BaseEntity) o;
        return id != null && Objects.equals(id, autre.id);
    }

    @Override
    public final int hashCode() {
        return classeReelle(this).hashCode();
    }

    private static Class<?> classeReelle(Object o) {
        return (o instanceof HibernateProxy proxy)
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
    }
}
