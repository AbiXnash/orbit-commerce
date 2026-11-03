package com.orbit.inventory.transfer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByCode(String code);

    boolean existsByCode(String code);

    List<Transfer> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Transfer x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Transfer> search(@Param("text") String text);
}
