package com.orbit.inventory.stockitem;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    Optional<StockItem> findByCode(String code);

    boolean existsByCode(String code);

    List<StockItem> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from StockItem x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<StockItem> search(@Param("text") String text);
}
