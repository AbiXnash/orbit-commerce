package com.orbit.customer.customer;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCode(String code);

    boolean existsByCode(String code);

    List<Customer> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Customer x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Customer> search(@Param("text") String text);
}
