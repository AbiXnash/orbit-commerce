package com.orbit.notification.suppression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class SuppressionRepositoryTest {

    @Autowired
    private SuppressionRepository repository;

    private Suppression save(String code, String name, int priority) {
        Suppression item = new Suppression(code, name);
        item.setPriority(priority);
        item.setAmount(BigDecimal.TEN);
        item.setEmail(code.toLowerCase() + "@example.com");
        item.setReason("hard-bounce");
        item.setExpiresAt(Instant.parse("2026-01-01T00:00:00Z"));
        return repository.save(item);
    }

    @Test
    void savesAndFindsByCode() {
        save("SUP-1", "Bounced address", 1);
        assertEquals("SUP-1", repository.findByCode("SUP-1").orElseThrow().getCode());
    }

    @Test
    void searchesByName() {
        save("SUP-1", "Bounced address", 1);
        save("SUP-2", "Complaint", 1);
        assertEquals(1, repository.search("bounced").size());
    }

    @Test
    void listsActiveSuppressions() {
        save("SUP-1", "One", 1);
        assertEquals(1, repository.findByStatus("ACTIVE").size());
    }

    @Test
    void rejectsADuplicateCode() {
        save("SUP-1", "One", 1);
        Suppression duplicate = new Suppression("SUP-1", "Two");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }
}
