package com.orbit.notification.digest;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DigestRepository extends JpaRepository<Digest, Long> {

    Optional<Digest> findByCode(String code);

    boolean existsByCode(String code);

    List<Digest> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Digest x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Digest> search(@Param("text") String text);
}
