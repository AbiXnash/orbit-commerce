package com.orbit.shipping.trackingevent;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    Optional<TrackingEvent> findByCode(String code);

    boolean existsByCode(String code);

    List<TrackingEvent> findByStatus(String status);

    List<TrackingEvent> findByStatusOrderByPriorityDesc(String status);

    List<TrackingEvent> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from TrackingEvent x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<TrackingEvent> search(@Param("text") String text);
}
