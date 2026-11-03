package com.orbit.payment.dispute;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    Optional<Dispute> findByCode(String code);

    boolean existsByCode(String code);

    List<Dispute> findByStatus(String status);

    List<Dispute> findByStatusOrderByPriorityDesc(String status);

    List<Dispute> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Dispute x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Dispute> search(@Param("text") String text);
}
