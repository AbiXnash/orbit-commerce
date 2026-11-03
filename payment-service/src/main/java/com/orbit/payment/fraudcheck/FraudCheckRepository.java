package com.orbit.payment.fraudcheck;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FraudCheckRepository extends JpaRepository<FraudCheck, Long> {

    Optional<FraudCheck> findByCode(String code);

    boolean existsByCode(String code);

    List<FraudCheck> findByStatus(String status);

    List<FraudCheck> findByStatusOrderByPriorityDesc(String status);

    List<FraudCheck> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from FraudCheck x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<FraudCheck> search(@Param("text") String text);
}
