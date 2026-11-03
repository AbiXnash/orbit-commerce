package com.orbit.customer.feedback;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByCode(String code);

    boolean existsByCode(String code);

    List<Feedback> findByStatus(String status);

    List<Feedback> findByStatusOrderByPriorityDesc(String status);

    List<Feedback> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Feedback x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Feedback> search(@Param("text") String text);
}
