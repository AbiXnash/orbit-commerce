package com.orbit.shipping.route;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByCode(String code);

    boolean existsByCode(String code);

    List<Route> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Route x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Route> search(@Param("text") String text);
}
