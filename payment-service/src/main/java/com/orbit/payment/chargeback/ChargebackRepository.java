package com.orbit.payment.chargeback;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargebackRepository extends JpaRepository<Chargeback, Long> {

    Optional<Chargeback> findByCode(String code);

    boolean existsByCode(String code);

    List<Chargeback> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Chargeback x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Chargeback> search(@Param("text") String text);
}
