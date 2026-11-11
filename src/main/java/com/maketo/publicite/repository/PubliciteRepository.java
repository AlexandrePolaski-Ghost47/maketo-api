package com.maketo.publicite.repository;

import com.maketo.publicite.entity.Publicite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface PubliciteRepository extends JpaRepository<Publicite, Long> {

    @Query("""
            SELECT p FROM Publicite p
            WHERE p.actif = true AND p.dateDebut <= :jour AND p.dateFin >= :jour
            ORDER BY p.dateCreation DESC
            """)
    List<Publicite> actives(@Param("jour") LocalDate jour);

    Page<Publicite> findAllByOrderByDateCreationDesc(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Publicite p SET p.nbClics = p.nbClics + 1 WHERE p.id = :id AND p.actif = true")
    int incrementerClics(@Param("id") Long id);
}
