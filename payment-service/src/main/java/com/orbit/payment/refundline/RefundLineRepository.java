package com.orbit.payment.refundline;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundLineRepository extends JpaRepository<RefundLine, Long> {

    Optional<RefundLine> findByCode(String code);

    boolean existsByCode(String code);

    List<RefundLine> findByStatus(String status);

    List<RefundLine> findByStatusOrderByPriorityDesc(String status);

    List<RefundLine> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from RefundLine x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<RefundLine> search(@Param("text") String text);
}
