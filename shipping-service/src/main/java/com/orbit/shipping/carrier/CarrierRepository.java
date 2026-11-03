package com.orbit.shipping.carrier;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {

    Optional<Carrier> findByCode(String code);

    boolean existsByCode(String code);

    List<Carrier> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Carrier x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Carrier> search(@Param("text") String text);
}
