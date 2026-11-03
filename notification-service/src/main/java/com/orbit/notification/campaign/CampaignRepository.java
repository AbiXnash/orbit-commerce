package com.orbit.notification.campaign;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByCode(String code);

    boolean existsByCode(String code);

    List<Campaign> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Campaign x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Campaign> search(@Param("text") String text);
}
