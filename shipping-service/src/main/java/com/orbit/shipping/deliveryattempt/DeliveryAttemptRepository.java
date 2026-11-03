package com.orbit.shipping.deliveryattempt;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {

    Optional<DeliveryAttempt> findByCode(String code);

    boolean existsByCode(String code);

    List<DeliveryAttempt> findByStatus(String status);

    List<DeliveryAttempt> findByStatusOrderByPriorityDesc(String status);

    List<DeliveryAttempt> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from DeliveryAttempt x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<DeliveryAttempt> search(@Param("text") String text);
}
