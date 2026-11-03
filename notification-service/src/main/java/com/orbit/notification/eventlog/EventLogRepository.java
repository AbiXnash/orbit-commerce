package com.orbit.notification.eventlog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {

    Optional<EventLog> findByCode(String code);

    boolean existsByCode(String code);

    List<EventLog> findByStatus(String status);

    List<EventLog> findByStatusOrderByPriorityDesc(String status);

    List<EventLog> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from EventLog x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<EventLog> search(@Param("text") String text);
}
