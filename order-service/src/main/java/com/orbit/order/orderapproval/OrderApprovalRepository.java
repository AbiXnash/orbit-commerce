package com.orbit.order.orderapproval;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderApprovalRepository extends JpaRepository<OrderApproval, Long> {

    Optional<OrderApproval> findByCode(String code);

    boolean existsByCode(String code);

    List<OrderApproval> findByStatus(String status);

    List<OrderApproval> findByStatusOrderByPriorityDesc(String status);

    List<OrderApproval> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from OrderApproval x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<OrderApproval> search(@Param("text") String text);
}
