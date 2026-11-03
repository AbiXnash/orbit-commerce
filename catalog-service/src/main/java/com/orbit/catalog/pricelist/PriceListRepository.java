package com.orbit.catalog.pricelist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    Optional<PriceList> findByCode(String code);

    boolean existsByCode(String code);

    List<PriceList> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from PriceList x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PriceList> search(@Param("text") String text);
}
