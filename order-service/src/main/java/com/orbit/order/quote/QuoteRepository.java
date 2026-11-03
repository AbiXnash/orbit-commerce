package com.orbit.order.quote;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Optional<Quote> findByCode(String code);

    boolean existsByCode(String code);

    List<Quote> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Quote x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Quote> search(@Param("text") String text);
}
