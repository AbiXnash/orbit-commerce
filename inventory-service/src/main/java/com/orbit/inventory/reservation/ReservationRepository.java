package com.orbit.inventory.reservation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByCode(String code);

    boolean existsByCode(String code);

    List<Reservation> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Reservation x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Reservation> search(@Param("text") String text);
}
