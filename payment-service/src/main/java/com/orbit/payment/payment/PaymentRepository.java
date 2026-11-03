package com.orbit.payment.payment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCode(String code);

    boolean existsByCode(String code);

    List<Payment> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Payment x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Payment> search(@Param("text") String text);
}
