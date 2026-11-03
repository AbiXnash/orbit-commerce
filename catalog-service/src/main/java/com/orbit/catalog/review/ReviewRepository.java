package com.orbit.catalog.review;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByCode(String code);

    boolean existsByCode(String code);

    List<Review> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Review x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Review> search(@Param("text") String text);
}
