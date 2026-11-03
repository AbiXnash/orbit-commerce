package com.orbit.payment.currencyrate;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    Optional<CurrencyRate> findByCode(String code);

    boolean existsByCode(String code);

    List<CurrencyRate> findByStatus(String status);

    List<CurrencyRate> findByStatusOrderByPriorityDesc(String status);

    List<CurrencyRate> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CurrencyRate x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CurrencyRate> search(@Param("text") String text);
}
