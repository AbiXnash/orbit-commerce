package com.orbit.notification.sendattempt;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SendAttemptRepository extends JpaRepository<SendAttempt, Long> {

    Optional<SendAttempt> findByCode(String code);

    boolean existsByCode(String code);

    List<SendAttempt> findByStatus(String status);

    List<SendAttempt> findByStatusOrderByPriorityDesc(String status);

    List<SendAttempt> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from SendAttempt x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<SendAttempt> search(@Param("text") String text);
}
