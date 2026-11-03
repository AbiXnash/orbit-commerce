package com.orbit.notification.notificationpreference;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByCode(String code);

    boolean existsByCode(String code);

    List<NotificationPreference> findByStatus(String status);

    List<NotificationPreference> findByStatusOrderByPriorityDesc(String status);

    List<NotificationPreference> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from NotificationPreference x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<NotificationPreference> search(@Param("text") String text);
}
