package com.orbit.payment.payoutbatch;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PayoutBatchRepository extends JpaRepository<PayoutBatch, Long> {

    Optional<PayoutBatch> findByCode(String code);

    boolean existsByCode(String code);

    List<PayoutBatch> findByStatus(String status);

    List<PayoutBatch> findByStatusOrderByPriorityDesc(String status);

    List<PayoutBatch> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from PayoutBatch x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PayoutBatch> search(@Param("text") String text);
}
