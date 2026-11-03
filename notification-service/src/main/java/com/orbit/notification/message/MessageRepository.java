package com.orbit.notification.message;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByCode(String code);

    boolean existsByCode(String code);

    List<Message> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Message x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Message> search(@Param("text") String text);
}
