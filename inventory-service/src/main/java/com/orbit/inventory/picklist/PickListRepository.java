package com.orbit.inventory.picklist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PickListRepository extends JpaRepository<PickList, Long> {

    Optional<PickList> findByCode(String code);

    boolean existsByCode(String code);

    List<PickList> findByStatus(String status);

    List<PickList> findByStatusOrderByPriorityDesc(String status);

    List<PickList> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from PickList x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PickList> search(@Param("text") String text);
}
