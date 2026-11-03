package com.orbit.order.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByCode(String code);

    boolean existsByCode(String code);

    List<Order> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Order x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Order> search(@Param("text") String text);
}
