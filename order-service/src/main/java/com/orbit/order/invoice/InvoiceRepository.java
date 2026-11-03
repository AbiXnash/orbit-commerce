package com.orbit.order.invoice;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByCode(String code);

    boolean existsByCode(String code);

    List<Invoice> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Invoice x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Invoice> search(@Param("text") String text);
}
