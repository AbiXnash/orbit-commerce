package com.orbit.notification.deliveryreport;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryReportRepository extends JpaRepository<DeliveryReport, Long> {

    Optional<DeliveryReport> findByCode(String code);

    boolean existsByCode(String code);

    List<DeliveryReport> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from DeliveryReport x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<DeliveryReport> search(@Param("text") String text);
}
