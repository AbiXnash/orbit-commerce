package com.orbit.payment.refund;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByCode(String code);

    boolean existsByCode(String code);

    List<Refund> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Refund x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Refund> search(@Param("text") String text);
}
