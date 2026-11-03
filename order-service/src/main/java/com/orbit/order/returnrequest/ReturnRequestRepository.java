package com.orbit.order.returnrequest;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    Optional<ReturnRequest> findByCode(String code);

    boolean existsByCode(String code);

    List<ReturnRequest> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from ReturnRequest x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<ReturnRequest> search(@Param("text") String text);
}
