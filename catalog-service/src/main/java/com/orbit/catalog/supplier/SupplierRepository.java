package com.orbit.catalog.supplier;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCode(String code);

    boolean existsByCode(String code);

    List<Supplier> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Supplier x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Supplier> search(@Param("text") String text);
}
