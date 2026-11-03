package com.orbit.payment.taxrule;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {

    Optional<TaxRule> findByCode(String code);

    boolean existsByCode(String code);

    List<TaxRule> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from TaxRule x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<TaxRule> search(@Param("text") String text);
}
