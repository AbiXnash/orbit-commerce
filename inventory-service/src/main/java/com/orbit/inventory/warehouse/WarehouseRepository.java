package com.orbit.inventory.warehouse;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByCode(String code);

    boolean existsByCode(String code);

    List<Warehouse> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Warehouse x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Warehouse> search(@Param("text") String text);
}
