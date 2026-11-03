package com.orbit.catalog.bundle;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BundleRepository extends JpaRepository<Bundle, Long> {

    Optional<Bundle> findByCode(String code);

    boolean existsByCode(String code);

    List<Bundle> findByStatus(String status);

    List<Bundle> findByStatusOrderByPriorityDesc(String status);

    List<Bundle> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Bundle x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Bundle> search(@Param("text") String text);
}
