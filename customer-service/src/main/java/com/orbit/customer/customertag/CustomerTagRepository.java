package com.orbit.customer.customertag;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerTagRepository extends JpaRepository<CustomerTag, Long> {

    Optional<CustomerTag> findByCode(String code);

    boolean existsByCode(String code);

    List<CustomerTag> findByStatus(String status);

    List<CustomerTag> findByStatusOrderByPriorityDesc(String status);

    List<CustomerTag> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CustomerTag x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CustomerTag> search(@Param("text") String text);
}
