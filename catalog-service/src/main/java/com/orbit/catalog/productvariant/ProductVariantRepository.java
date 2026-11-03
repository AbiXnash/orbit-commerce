package com.orbit.catalog.productvariant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByCode(String code);

    boolean existsByCode(String code);

    List<ProductVariant> findByStatus(String status);

    List<ProductVariant> findByStatusOrderByPriorityDesc(String status);

    List<ProductVariant> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from ProductVariant x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<ProductVariant> search(@Param("text") String text);
}
