package com.orbit.shipping.dispatch;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    Optional<Dispatch> findByCode(String code);

    boolean existsByCode(String code);

    List<Dispatch> findByStatus(String status);

    List<Dispatch> findByStatusOrderByPriorityDesc(String status);

    List<Dispatch> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Dispatch x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Dispatch> search(@Param("text") String text);
}
