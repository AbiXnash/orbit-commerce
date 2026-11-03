package com.orbit.payment.merchantaccount;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantAccountRepository extends JpaRepository<MerchantAccount, Long> {

    Optional<MerchantAccount> findByCode(String code);

    boolean existsByCode(String code);

    List<MerchantAccount> findByStatus(String status);

    List<MerchantAccount> findByStatusOrderByPriorityDesc(String status);

    List<MerchantAccount> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from MerchantAccount x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<MerchantAccount> search(@Param("text") String text);
}
