package com.orbit.catalog.seoentry;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeoEntryRepository extends JpaRepository<SeoEntry, Long> {

    Optional<SeoEntry> findByCode(String code);

    boolean existsByCode(String code);

    List<SeoEntry> findByStatus(String status);

    List<SeoEntry> findByStatusOrderByPriorityDesc(String status);

    List<SeoEntry> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from SeoEntry x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<SeoEntry> search(@Param("text") String text);
}
