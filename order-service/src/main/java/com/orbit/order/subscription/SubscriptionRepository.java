package com.orbit.order.subscription;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByCode(String code);

    boolean existsByCode(String code);

    List<Subscription> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Subscription x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Subscription> search(@Param("text") String text);
}
