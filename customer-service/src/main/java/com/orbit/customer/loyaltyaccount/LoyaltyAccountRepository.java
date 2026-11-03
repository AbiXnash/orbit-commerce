package com.orbit.customer.loyaltyaccount;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {

    Optional<LoyaltyAccount> findByCode(String code);

    boolean existsByCode(String code);

    List<LoyaltyAccount> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from LoyaltyAccount x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<LoyaltyAccount> search(@Param("text") String text);
}
