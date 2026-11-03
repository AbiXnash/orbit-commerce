package com.orbit.inventory.lot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class LotRepositoryTest {

    @Autowired
    private LotRepository repository;

    private Lot save(String code, String name, int priority) {
        Lot item = new Lot(code, name);
        item.setPriority(priority);
        item.setAmount(BigDecimal.TEN);
        item.setExpiryDate(Instant.parse("2025-01-01T00:00:00Z"));
        return repository.save(item);
    }

    @Test
    void savesAndFindsByCode() {
        save("LOT-1", "Spring batch", 3);
        assertEquals("LOT-1", repository.findByCode("LOT-1").orElseThrow().getCode());
    }

    @Test
    void searchesByName() {
        save("LOT-1", "Spring batch", 3);
        save("LOT-2", "Winter batch", 1);
        assertEquals(1, repository.search("spring").size());
    }

    @Test
    void findsHighPriorityLots() {
        save("LOT-1", "Low", 1);
        save("LOT-2", "High", 9);
        assertEquals("LOT-2", repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(5).get(0).getCode());
    }

    @Test
    void rejectsADuplicateCode() {
        save("LOT-1", "One", 1);
        Lot duplicate = new Lot("LOT-1", "Two");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }
}
