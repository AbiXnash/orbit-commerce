package com.orbit.catalog.discountrule;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiscountRuleRepository extends JpaRepository<DiscountRule, Long> {

    Optional<DiscountRule> findByCode(String code);

    boolean existsByCode(String code);

    List<DiscountRule> findByStatus(String status);

    List<DiscountRule> findByStatusOrderByPriorityDesc(String status);

    List<DiscountRule> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from DiscountRule x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<DiscountRule> search(@Param("text") String text);
}
