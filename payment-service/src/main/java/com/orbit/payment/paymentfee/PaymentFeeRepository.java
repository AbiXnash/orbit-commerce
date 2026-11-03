package com.orbit.payment.paymentfee;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentFeeRepository extends JpaRepository<PaymentFee, Long> {

    Optional<PaymentFee> findByCode(String code);

    boolean existsByCode(String code);

    List<PaymentFee> findByStatus(String status);

    List<PaymentFee> findByStatusOrderByPriorityDesc(String status);

    List<PaymentFee> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from PaymentFee x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PaymentFee> search(@Param("text") String text);
}
