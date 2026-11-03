package com.orbit.notification.newsletter;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    Optional<Newsletter> findByCode(String code);

    boolean existsByCode(String code);

    List<Newsletter> findByStatus(String status);

    List<Newsletter> findByStatusOrderByPriorityDesc(String status);

    List<Newsletter> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Newsletter x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Newsletter> search(@Param("text") String text);
}
