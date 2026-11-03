package com.orbit.inventory.putawaytask;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PutawayTaskRepository extends JpaRepository<PutawayTask, Long> {

    Optional<PutawayTask> findByCode(String code);

    boolean existsByCode(String code);

    List<PutawayTask> findByStatus(String status);

    List<PutawayTask> findByStatusOrderByPriorityDesc(String status);

    List<PutawayTask> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from PutawayTask x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PutawayTask> search(@Param("text") String text);
}
