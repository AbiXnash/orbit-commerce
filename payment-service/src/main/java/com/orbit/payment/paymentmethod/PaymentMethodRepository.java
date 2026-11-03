package com.orbit.payment.paymentmethod;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findByCode(String code);

    boolean existsByCode(String code);

    List<PaymentMethod> findByStatus(String status);

    List<PaymentMethod> findByStatusOrderByPriorityDesc(String status);

    List<PaymentMethod> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from PaymentMethod x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PaymentMethod> search(@Param("text") String text);
}
