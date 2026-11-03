package com.orbit.payment.settlement;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByCode(String code);

    boolean existsByCode(String code);

    List<Settlement> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Settlement x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Settlement> search(@Param("text") String text);
}
