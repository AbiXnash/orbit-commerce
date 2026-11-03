package com.orbit.notification.template;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByCode(String code);

    boolean existsByCode(String code);

    List<Template> findByStatus(String status);

    long countByStatus(String status);

    @Query("select x from Template x where lower(x.name) like lower(concat('%', :text, '%')) order by x.priority desc")
    List<Template> search(@Param("text") String text);
}
