package com.orbit.notification.pushsubscription;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    Optional<PushSubscription> findByCode(String code);

    boolean existsByCode(String code);

    List<PushSubscription> findByStatus(String status);

    List<PushSubscription> findByStatusOrderByPriorityDesc(String status);

    List<PushSubscription> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from PushSubscription x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PushSubscription> search(@Param("text") String text);
}
