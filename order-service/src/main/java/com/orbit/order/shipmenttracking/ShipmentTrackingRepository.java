package com.orbit.order.shipmenttracking;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Long> {

    Optional<ShipmentTracking> findByCode(String code);

    boolean existsByCode(String code);

    List<ShipmentTracking> findByStatus(String status);

    List<ShipmentTracking> findByStatusOrderByPriorityDesc(String status);

    List<ShipmentTracking> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from ShipmentTracking x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<ShipmentTracking> search(@Param("text") String text);
}
