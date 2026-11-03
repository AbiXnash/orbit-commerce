package com.orbit.customer.preference;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {

    Optional<Preference> findByCode(String code);

    boolean existsByCode(String code);

    List<Preference> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Preference x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Preference> search(@Param("text") String text);
}
