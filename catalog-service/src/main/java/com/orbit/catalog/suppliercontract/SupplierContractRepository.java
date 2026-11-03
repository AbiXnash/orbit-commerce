package com.orbit.catalog.suppliercontract;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierContractRepository extends JpaRepository<SupplierContract, Long> {

    Optional<SupplierContract> findByCode(String code);

    boolean existsByCode(String code);

    List<SupplierContract> findByStatus(String status);

    List<SupplierContract> findByStatusOrderByPriorityDesc(String status);

    List<SupplierContract> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from SupplierContract x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<SupplierContract> search(@Param("text") String text);
}
