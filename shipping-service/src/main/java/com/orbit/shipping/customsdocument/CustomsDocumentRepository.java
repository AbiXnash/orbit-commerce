package com.orbit.shipping.customsdocument;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomsDocumentRepository extends JpaRepository<CustomsDocument, Long> {

    Optional<CustomsDocument> findByCode(String code);

    boolean existsByCode(String code);

    List<CustomsDocument> findByStatus(String status);

    List<CustomsDocument> findByStatusOrderByPriorityDesc(String status);

    List<CustomsDocument> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CustomsDocument x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CustomsDocument> search(@Param("text") String text);
}
