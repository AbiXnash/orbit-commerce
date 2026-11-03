package com.orbit.customer.referral;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReferralRepository extends JpaRepository<Referral, Long> {

    Optional<Referral> findByCode(String code);

    boolean existsByCode(String code);

    List<Referral> findByStatus(String status);

    List<Referral> findByStatusOrderByPriorityDesc(String status);

    List<Referral> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from Referral x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Referral> search(@Param("text") String text);
}
