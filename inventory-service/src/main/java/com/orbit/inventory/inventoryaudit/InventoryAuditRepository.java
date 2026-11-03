package com.orbit.inventory.inventoryaudit;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, Long> {

    Optional<InventoryAudit> findByCode(String code);

    boolean existsByCode(String code);

    List<InventoryAudit> findByStatus(String status);

    List<InventoryAudit> findByStatusOrderByPriorityDesc(String status);

    List<InventoryAudit> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from InventoryAudit x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<InventoryAudit> search(@Param("text") String text);
}
