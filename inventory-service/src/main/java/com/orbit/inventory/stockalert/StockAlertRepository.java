package com.orbit.inventory.stockalert;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    Optional<StockAlert> findByCode(String code);

    boolean existsByCode(String code);

    List<StockAlert> findByStatus(String status);

    List<StockAlert> findByStatusOrderByPriorityDesc(String status);

    List<StockAlert> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from StockAlert x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<StockAlert> search(@Param("text") String text);
}
