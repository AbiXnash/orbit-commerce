package com.orbit.inventory.backorder;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BackorderRepository extends JpaRepository<Backorder, Long> {

    Optional<Backorder> findByCode(String code);

    boolean existsByCode(String code);

    List<Backorder> findByStatus(String status);

    List<Backorder> findByStatusOrderByPriorityDesc(String status);

    List<Backorder> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Backorder x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Backorder> search(@Param("text") String text);
}
