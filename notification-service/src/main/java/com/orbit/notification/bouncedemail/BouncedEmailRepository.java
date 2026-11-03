package com.orbit.notification.bouncedemail;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BouncedEmailRepository extends JpaRepository<BouncedEmail, Long> {

    Optional<BouncedEmail> findByCode(String code);

    boolean existsByCode(String code);

    List<BouncedEmail> findByStatus(String status);

    List<BouncedEmail> findByStatusOrderByPriorityDesc(String status);

    List<BouncedEmail> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from BouncedEmail x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<BouncedEmail> search(@Param("text") String text);
}
