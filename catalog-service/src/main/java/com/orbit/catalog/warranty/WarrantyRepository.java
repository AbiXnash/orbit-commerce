package com.orbit.catalog.warranty;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarrantyRepository extends JpaRepository<Warranty, Long> {

    Optional<Warranty> findByCode(String code);

    boolean existsByCode(String code);

    List<Warranty> findByStatus(String status);

    List<Warranty> findByStatusOrderByPriorityDesc(String status);

    List<Warranty> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Warranty x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Warranty> search(@Param("text") String text);
}
