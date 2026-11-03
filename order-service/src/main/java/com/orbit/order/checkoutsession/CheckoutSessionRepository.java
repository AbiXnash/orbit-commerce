package com.orbit.order.checkoutsession;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {

    Optional<CheckoutSession> findByCode(String code);

    boolean existsByCode(String code);

    List<CheckoutSession> findByStatus(String status);

    List<CheckoutSession> findByStatusOrderByPriorityDesc(String status);

    List<CheckoutSession> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CheckoutSession x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CheckoutSession> search(@Param("text") String text);
}
