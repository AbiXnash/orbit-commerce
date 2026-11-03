package com.orbit.customer.consent;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsentRepository extends JpaRepository<Consent, Long> {

    Optional<Consent> findByCode(String code);

    boolean existsByCode(String code);

    List<Consent> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Consent x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Consent> search(@Param("text") String text);
}
