package com.orbit.shipping.rate;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RateRepository extends JpaRepository<Rate, Long> {

    Optional<Rate> findByCode(String code);

    boolean existsByCode(String code);

    List<Rate> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Rate x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Rate> search(@Param("text") String text);
}
