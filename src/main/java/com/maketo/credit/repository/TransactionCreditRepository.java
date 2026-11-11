package com.maketo.credit.repository;

import com.maketo.credit.entity.TransactionCredit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCreditRepository extends JpaRepository<TransactionCredit, Long> {

    Page<TransactionCredit> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId, Pageable pageable);

    boolean existsByReferenceMomo(String referenceMomo);
}
