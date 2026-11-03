package com.orbit.inventory.lot;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LotRepository extends JpaRepository<Lot, Long> {

    Optional<Lot> findByCode(String code);

    boolean existsByCode(String code);

    List<Lot> findByStatus(String status);

    List<Lot> findByStatusOrderByPriorityDesc(String status);

    List<Lot> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Lot x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Lot> search(@Param("text") String text);
}
