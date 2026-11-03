package com.orbit.notification.announcement;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Optional<Announcement> findByCode(String code);

    boolean existsByCode(String code);

    List<Announcement> findByStatus(String status);

    List<Announcement> findByStatusOrderByPriorityDesc(String status);

    List<Announcement> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Announcement x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Announcement> search(@Param("text") String text);
}
