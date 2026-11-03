package com.orbit.customer.customergroup;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {

    Optional<CustomerGroup> findByCode(String code);

    boolean existsByCode(String code);

    List<CustomerGroup> findByStatus(String status);

    List<CustomerGroup> findByStatusOrderByPriorityDesc(String status);

    List<CustomerGroup> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CustomerGroup x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CustomerGroup> search(@Param("text") String text);
}
