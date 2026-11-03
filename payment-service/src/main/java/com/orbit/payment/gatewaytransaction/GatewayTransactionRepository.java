package com.orbit.payment.gatewaytransaction;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatewayTransactionRepository extends JpaRepository<GatewayTransaction, Long> {

    Optional<GatewayTransaction> findByCode(String code);

    boolean existsByCode(String code);

    List<GatewayTransaction> findByStatus(String status);

    List<GatewayTransaction> findByStatusOrderByPriorityDesc(String status);

    List<GatewayTransaction> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from GatewayTransaction x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<GatewayTransaction> search(@Param("text") String text);
}
