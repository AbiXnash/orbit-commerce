package com.orbit.inventory.serialnumber;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {

    Optional<SerialNumber> findByCode(String code);

    boolean existsByCode(String code);

    List<SerialNumber> findByStatus(String status);

    List<SerialNumber> findByStatusOrderByPriorityDesc(String status);

    List<SerialNumber> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from SerialNumber x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<SerialNumber> search(@Param("text") String text);
}
