package com.orbit.shipping.delivery;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByCode(String code);

    boolean existsByCode(String code);

    List<Delivery> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Delivery x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Delivery> search(@Param("text") String text);
}
