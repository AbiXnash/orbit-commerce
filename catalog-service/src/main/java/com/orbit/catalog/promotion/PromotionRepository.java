package com.orbit.catalog.promotion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCode(String code);

    boolean existsByCode(String code);

    List<Promotion> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Promotion x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Promotion> search(@Param("text") String text);
}
