package com.orbit.order.layaway;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LayawayRepository extends JpaRepository<Layaway, Long> {

    Optional<Layaway> findByCode(String code);

    boolean existsByCode(String code);

    List<Layaway> findByStatus(String status);

    List<Layaway> findByStatusOrderByPriorityDesc(String status);

    List<Layaway> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Layaway x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Layaway> search(@Param("text") String text);
}
