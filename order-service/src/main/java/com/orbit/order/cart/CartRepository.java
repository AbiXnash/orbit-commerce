package com.orbit.order.cart;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCode(String code);

    boolean existsByCode(String code);

    List<Cart> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Cart x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Cart> search(@Param("text") String text);
}
