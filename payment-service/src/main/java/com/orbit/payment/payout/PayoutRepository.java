package com.orbit.payment.payout;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    Optional<Payout> findByCode(String code);

    boolean existsByCode(String code);

    List<Payout> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Payout x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Payout> search(@Param("text") String text);
}
