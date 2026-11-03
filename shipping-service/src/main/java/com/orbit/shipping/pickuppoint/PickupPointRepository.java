package com.orbit.shipping.pickuppoint;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PickupPointRepository extends JpaRepository<PickupPoint, Long> {

    Optional<PickupPoint> findByCode(String code);

    boolean existsByCode(String code);

    List<PickupPoint> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from PickupPoint x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PickupPoint> search(@Param("text") String text);
}
