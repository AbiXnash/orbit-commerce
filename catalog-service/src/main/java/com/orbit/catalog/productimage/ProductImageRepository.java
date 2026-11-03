package com.orbit.catalog.productimage;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    Optional<ProductImage> findByCode(String code);

    boolean existsByCode(String code);

    List<ProductImage> findByStatus(String status);

    List<ProductImage> findByStatusOrderByPriorityDesc(String status);

    List<ProductImage> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from ProductImage x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<ProductImage> search(@Param("text") String text);
}
