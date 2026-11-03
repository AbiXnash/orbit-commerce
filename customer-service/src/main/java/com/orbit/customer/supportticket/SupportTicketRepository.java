package com.orbit.customer.supportticket;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByCode(String code);

    boolean existsByCode(String code);

    List<SupportTicket> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from SupportTicket x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<SupportTicket> search(@Param("text") String text);
}
