package com.orbit.notification.webhook;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {

    Optional<Webhook> findByCode(String code);

    boolean existsByCode(String code);

    List<Webhook> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Webhook x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Webhook> search(@Param("text") String text);
}
