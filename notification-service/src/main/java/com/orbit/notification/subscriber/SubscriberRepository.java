package com.orbit.notification.subscriber;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByCode(String code);

    boolean existsByCode(String code);

    List<Subscriber> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Subscriber x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Subscriber> search(@Param("text") String text);
}
