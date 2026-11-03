package com.orbit.order.orderline;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    Optional<OrderLine> findByCode(String code);

    boolean existsByCode(String code);

    List<OrderLine> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from OrderLine x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<OrderLine> search(@Param("text") String text);
}
