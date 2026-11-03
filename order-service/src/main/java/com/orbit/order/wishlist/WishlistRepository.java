package com.orbit.order.wishlist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByCode(String code);

    boolean existsByCode(String code);

    List<Wishlist> findByStatus(String status);

    List<Wishlist> findByStatusOrderByPriorityDesc(String status);

    List<Wishlist> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Wishlist x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Wishlist> search(@Param("text") String text);
}
