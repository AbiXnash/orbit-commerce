package com.orbit.customer.identitydocument;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, Long> {

    Optional<IdentityDocument> findByCode(String code);

    boolean existsByCode(String code);

    List<IdentityDocument> findByStatus(String status);

    List<IdentityDocument> findByStatusOrderByPriorityDesc(String status);

    List<IdentityDocument> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from IdentityDocument x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<IdentityDocument> search(@Param("text") String text);
}
