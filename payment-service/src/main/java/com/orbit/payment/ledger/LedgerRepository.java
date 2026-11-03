package com.orbit.payment.ledger;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    Optional<Ledger> findByCode(String code);

    boolean existsByCode(String code);

    List<Ledger> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Ledger x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Ledger> search(@Param("text") String text);
}
