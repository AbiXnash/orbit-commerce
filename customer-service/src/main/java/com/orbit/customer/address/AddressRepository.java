package com.orbit.customer.address;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByCode(String code);

    boolean existsByCode(String code);

    List<Address> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Address x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Address> search(@Param("text") String text);
}
