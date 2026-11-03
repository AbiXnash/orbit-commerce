package com.orbit.customer.surveyresponse;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    Optional<SurveyResponse> findByCode(String code);

    boolean existsByCode(String code);

    List<SurveyResponse> findByStatus(String status);

    List<SurveyResponse> findByStatusOrderByPriorityDesc(String status);

    List<SurveyResponse> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from SurveyResponse x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<SurveyResponse> search(@Param("text") String text);
}
