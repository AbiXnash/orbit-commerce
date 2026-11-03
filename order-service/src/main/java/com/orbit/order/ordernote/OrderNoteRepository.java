package com.orbit.order.ordernote;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderNoteRepository extends JpaRepository<OrderNote, Long> {

    Optional<OrderNote> findByCode(String code);

    boolean existsByCode(String code);

    List<OrderNote> findByStatus(String status);

    List<OrderNote> findByStatusOrderByPriorityDesc(String status);

    List<OrderNote> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from OrderNote x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<OrderNote> search(@Param("text") String text);
}
