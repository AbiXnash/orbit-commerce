package com.orbit.customer.customernote;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    Optional<CustomerNote> findByCode(String code);

    boolean existsByCode(String code);

    List<CustomerNote> findByStatus(String status);

    List<CustomerNote> findByStatusOrderByPriorityDesc(String status);

    List<CustomerNote> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CustomerNote x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CustomerNote> search(@Param("text") String text);
}
