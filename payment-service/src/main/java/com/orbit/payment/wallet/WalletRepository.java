package com.orbit.payment.wallet;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByCode(String code);

    boolean existsByCode(String code);

    List<Wallet> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Wallet x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Wallet> search(@Param("text") String text);
}
