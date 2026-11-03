package com.orbit.shipping.manifest;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManifestRepository extends JpaRepository<Manifest, Long> {

    Optional<Manifest> findByCode(String code);

    boolean existsByCode(String code);

    List<Manifest> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Manifest x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Manifest> search(@Param("text") String text);
}
