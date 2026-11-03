package com.orbit.catalog.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCode(String code);

    boolean existsByCode(String code);

    List<Category> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Category x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Category> search(@Param("text") String text);
}
