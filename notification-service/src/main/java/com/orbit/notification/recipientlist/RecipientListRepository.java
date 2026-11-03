package com.orbit.notification.recipientlist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipientListRepository extends JpaRepository<RecipientList, Long> {

    Optional<RecipientList> findByCode(String code);

    boolean existsByCode(String code);

    List<RecipientList> findByStatus(String status);

    List<RecipientList> findByStatusOrderByPriorityDesc(String status);

    List<RecipientList> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from RecipientList x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<RecipientList> search(@Param("text") String text);
}
