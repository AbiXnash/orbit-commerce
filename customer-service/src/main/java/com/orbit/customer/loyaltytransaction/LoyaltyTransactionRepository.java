package com.orbit.customer.loyaltytransaction;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    Optional<LoyaltyTransaction> findByCode(String code);

    boolean existsByCode(String code);

    List<LoyaltyTransaction> findByStatus(String status);

    List<LoyaltyTransaction> findByStatusOrderByPriorityDesc(String status);

    List<LoyaltyTransaction> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from LoyaltyTransaction x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<LoyaltyTransaction> search(@Param("text") String text);
}
