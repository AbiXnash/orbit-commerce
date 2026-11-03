package com.orbit.shipping.routeplan;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {

    Optional<RoutePlan> findByCode(String code);

    boolean existsByCode(String code);

    List<RoutePlan> findByStatus(String status);

    List<RoutePlan> findByStatusOrderByPriorityDesc(String status);

    List<RoutePlan> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from RoutePlan x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<RoutePlan> search(@Param("text") String text);
}
