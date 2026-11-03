package com.orbit.notification.channel;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByCode(String code);

    boolean existsByCode(String code);

    List<Channel> findByStatus(String status);

    List<Channel> findByStatusOrderByPriorityDesc(String status);

    List<Channel> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Channel x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Channel> search(@Param("text") String text);
}
