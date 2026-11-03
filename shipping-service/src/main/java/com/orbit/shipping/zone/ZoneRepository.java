package com.orbit.shipping.zone;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    Optional<Zone> findByCode(String code);

    boolean existsByCode(String code);

    List<Zone> findByStatus(String status);

    List<Zone> findByStatusOrderByPriorityDesc(String status);

    List<Zone> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Zone x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Zone> search(@Param("text") String text);
}
