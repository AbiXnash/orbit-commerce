package com.orbit.customer.communicationpreference;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunicationPreferenceRepository extends JpaRepository<CommunicationPreference, Long> {

    Optional<CommunicationPreference> findByCode(String code);

    boolean existsByCode(String code);

    List<CommunicationPreference> findByStatus(String status);

    List<CommunicationPreference> findByStatusOrderByPriorityDesc(String status);

    List<CommunicationPreference> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CommunicationPreference x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CommunicationPreference> search(@Param("text") String text);
}
