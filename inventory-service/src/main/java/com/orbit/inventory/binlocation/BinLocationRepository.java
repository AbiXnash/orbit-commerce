package com.orbit.inventory.binlocation;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BinLocationRepository extends JpaRepository<BinLocation, Long> {

    Optional<BinLocation> findByCode(String code);

    boolean existsByCode(String code);

    List<BinLocation> findByStatus(String status);

    List<BinLocation> findByStatusOrderByPriorityDesc(String status);

    List<BinLocation> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from BinLocation x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<BinLocation> search(@Param("text") String text);
}
