package com.orbit.order.preorder;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreorderRepository extends JpaRepository<Preorder, Long> {

    Optional<Preorder> findByCode(String code);

    boolean existsByCode(String code);

    List<Preorder> findByStatus(String status);

    List<Preorder> findByStatusOrderByPriorityDesc(String status);

    List<Preorder> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Preorder x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Preorder> search(@Param("text") String text);
}
