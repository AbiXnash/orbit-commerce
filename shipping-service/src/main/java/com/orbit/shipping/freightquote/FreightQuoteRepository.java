package com.orbit.shipping.freightquote;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FreightQuoteRepository extends JpaRepository<FreightQuote, Long> {

    Optional<FreightQuote> findByCode(String code);

    boolean existsByCode(String code);

    List<FreightQuote> findByStatus(String status);

    List<FreightQuote> findByStatusOrderByPriorityDesc(String status);

    List<FreightQuote> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from FreightQuote x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<FreightQuote> search(@Param("text") String text);
}
