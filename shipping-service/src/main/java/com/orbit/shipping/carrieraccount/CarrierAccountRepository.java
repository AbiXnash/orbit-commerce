package com.orbit.shipping.carrieraccount;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarrierAccountRepository extends JpaRepository<CarrierAccount, Long> {

    Optional<CarrierAccount> findByCode(String code);

    boolean existsByCode(String code);

    List<CarrierAccount> findByStatus(String status);

    List<CarrierAccount> findByStatusOrderByPriorityDesc(String status);

    List<CarrierAccount> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from CarrierAccount x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<CarrierAccount> search(@Param("text") String text);
}
