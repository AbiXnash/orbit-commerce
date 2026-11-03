package com.orbit.notification.suppression;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SuppressionRepository extends JpaRepository<Suppression, Long> {

    Optional<Suppression> findByCode(String code);

    boolean existsByCode(String code);

    List<Suppression> findByStatus(String status);

    List<Suppression> findByStatusOrderByPriorityDesc(String status);

    List<Suppression> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Suppression x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Suppression> search(@Param("text") String text);
}
