package com.orbit.catalog.producttag;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {

    Optional<ProductTag> findByCode(String code);

    boolean existsByCode(String code);

    List<ProductTag> findByStatus(String status);

    List<ProductTag> findByStatusOrderByPriorityDesc(String status);

    List<ProductTag> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from ProductTag x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<ProductTag> search(@Param("text") String text);
}
