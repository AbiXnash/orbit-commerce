package com.orbit.inventory.replenishment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReplenishmentRepository extends JpaRepository<Replenishment, Long> {

    Optional<Replenishment> findByCode(String code);

    boolean existsByCode(String code);

    List<Replenishment> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Replenishment x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Replenishment> search(@Param("text") String text);
}
