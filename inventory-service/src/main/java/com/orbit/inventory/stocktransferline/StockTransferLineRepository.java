package com.orbit.inventory.stocktransferline;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTransferLineRepository extends JpaRepository<StockTransferLine, Long> {

    Optional<StockTransferLine> findByCode(String code);

    boolean existsByCode(String code);

    List<StockTransferLine> findByStatus(String status);

    List<StockTransferLine> findByStatusOrderByPriorityDesc(String status);

    List<StockTransferLine> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from StockTransferLine x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<StockTransferLine> search(@Param("text") String text);
}
