package com.orbit.catalog.collection;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findByCode(String code);

    boolean existsByCode(String code);

    List<Collection> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Collection x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Collection> search(@Param("text") String text);
}
