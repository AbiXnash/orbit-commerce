package com.orbit.order.giftcard;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {

    Optional<GiftCard> findByCode(String code);

    boolean existsByCode(String code);

    List<GiftCard> findByStatus(String status);

    List<GiftCard> findByStatusOrderByPriorityDesc(String status);

    List<GiftCard> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from GiftCard x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<GiftCard> search(@Param("text") String text);
}
