package com.orbit.catalog.pricebook;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceBookRepository extends JpaRepository<PriceBook, Long> {

    Optional<PriceBook> findByCode(String code);

    boolean existsByCode(String code);

    List<PriceBook> findByStatus(String status);

    List<PriceBook> findByStatusOrderByPriorityDesc(String status);

    List<PriceBook> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from PriceBook x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PriceBook> search(@Param("text") String text);
}
