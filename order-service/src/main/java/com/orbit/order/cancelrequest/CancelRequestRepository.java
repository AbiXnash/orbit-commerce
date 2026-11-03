package com.orbit.order.cancelrequest;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CancelRequestRepository extends JpaRepository<CancelRequest, Long> {

    Optional<CancelRequest> findByCode(String code);

    boolean existsByCode(String code);

    List<CancelRequest> findByStatus(String status);

    List<CancelRequest> findByStatusOrderByPriorityDesc(String status);

    List<CancelRequest> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CancelRequest x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CancelRequest> search(@Param("text") String text);
}
