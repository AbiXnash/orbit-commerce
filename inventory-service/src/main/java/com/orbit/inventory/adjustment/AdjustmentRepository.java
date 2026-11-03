package com.orbit.inventory.adjustment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdjustmentRepository extends JpaRepository<Adjustment, Long> {

    Optional<Adjustment> findByCode(String code);

    boolean existsByCode(String code);

    List<Adjustment> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Adjustment x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Adjustment> search(@Param("text") String text);
}
