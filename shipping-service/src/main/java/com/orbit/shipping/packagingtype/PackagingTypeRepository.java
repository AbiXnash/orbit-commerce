package com.orbit.shipping.packagingtype;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PackagingTypeRepository extends JpaRepository<PackagingType, Long> {

    Optional<PackagingType> findByCode(String code);

    boolean existsByCode(String code);

    List<PackagingType> findByStatus(String status);

    List<PackagingType> findByStatusOrderByPriorityDesc(String status);

    List<PackagingType> findByPriorityGreaterThanEqualOrderByPriorityDesc(int priority);

    long countByStatus(String status);

    @Query("select x from PackagingType x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<PackagingType> search(@Param("text") String text);
}
