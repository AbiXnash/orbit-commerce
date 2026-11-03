package com.orbit.inventory.cyclecount;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CycleCountRepository extends JpaRepository<CycleCount, Long> {

    Optional<CycleCount> findByCode(String code);

    boolean existsByCode(String code);

    List<CycleCount> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from CycleCount x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CycleCount> search(@Param("text") String text);
}
