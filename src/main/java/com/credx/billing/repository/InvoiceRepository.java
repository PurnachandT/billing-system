package com.credx.billing.repository;

import com.credx.billing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByOrgIdOrderByBillingDateDesc(Long orgId);
}