package com.orbit.catalog.catalogimport;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CatalogImportRepository extends JpaRepository<CatalogImport, Long> {

    Optional<CatalogImport> findByCode(String code);

    boolean existsByCode(String code);

    List<CatalogImport> findByStatus(String status);

    List<CatalogImport> findByStatusOrderByPriorityDesc(String status);

    List<CatalogImport> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CatalogImport x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CatalogImport> search(@Param("text") String text);
}
