package com.orbit.inventory.shipment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByCode(String code);

    boolean existsByCode(String code);

    List<Shipment> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Shipment x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Shipment> search(@Param("text") String text);
}
