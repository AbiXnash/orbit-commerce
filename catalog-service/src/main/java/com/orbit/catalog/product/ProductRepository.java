package com.orbit.catalog.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    List<Product> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Product x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Product> search(@Param("text") String text);
}
