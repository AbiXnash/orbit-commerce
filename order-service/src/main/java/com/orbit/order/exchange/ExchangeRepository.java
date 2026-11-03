package com.orbit.order.exchange;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    Optional<Exchange> findByCode(String code);

    boolean existsByCode(String code);

    List<Exchange> findByStatus(String status);

    List<Exchange> findByStatusOrderByPriorityDesc(String status);

    List<Exchange> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Exchange x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Exchange> search(@Param("text") String text);
}
