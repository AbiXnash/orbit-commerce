package com.orbit.inventory.safetystock;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SafetyStockRepository extends JpaRepository<SafetyStock, Long> {

    Optional<SafetyStock> findByCode(String code);

    boolean existsByCode(String code);

    List<SafetyStock> findByStatus(String status);

    List<SafetyStock> findByStatusOrderByPriorityDesc(String status);

    List<SafetyStock> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from SafetyStock x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<SafetyStock> search(@Param("text") String text);
}
