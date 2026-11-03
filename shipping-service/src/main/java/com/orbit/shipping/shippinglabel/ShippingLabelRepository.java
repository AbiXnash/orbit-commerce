package com.orbit.shipping.shippinglabel;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShippingLabelRepository extends JpaRepository<ShippingLabel, Long> {

    Optional<ShippingLabel> findByCode(String code);

    boolean existsByCode(String code);

    List<ShippingLabel> findByStatus(String status);

    List<ShippingLabel> findByStatusOrderByPriorityDesc(String status);

    List<ShippingLabel> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from ShippingLabel x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<ShippingLabel> search(@Param("text") String text);
}
