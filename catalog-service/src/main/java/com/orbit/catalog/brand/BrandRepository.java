package com.orbit.catalog.brand;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByCode(String code);

    boolean existsByCode(String code);

    List<Brand> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Brand x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Brand> search(@Param("text") String text);
}
