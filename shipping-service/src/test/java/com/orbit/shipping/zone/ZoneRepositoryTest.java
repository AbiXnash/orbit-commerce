package com.orbit.shipping.zone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ZoneRepositoryTest {

    @Autowired
    private ZoneRepository repository;

    private Zone save(String code, String name, int priority) {
        Zone item = new Zone(code, name);
        item.setPriority(priority);
        item.setAmount(BigDecimal.TEN);
        item.setZoneCode(code);
        item.setRegion("EU");
        item.setPostalRange("1000-1999");
        return repository.save(item);
    }

    @Test
    void savesAndFindsByCode() {
        save("EU-1", "Central Europe", 1);
        assertEquals("EU-1", repository.findByCode("EU-1").orElseThrow().getCode());
    }

    @Test
    void searchesByName() {
        save("EU-1", "Central Europe", 1);
        save("EU-2", "Northern Europe", 1);
        assertEquals(1, repository.search("central").size());
    }

    @Test
    void countsActiveZones() {
        save("EU-1", "One", 1);
        assertEquals(1, repository.countByStatus("ACTIVE"));
    }

    @Test
    void rejectsADuplicateCode() {
        save("EU-1", "One", 1);
        Zone duplicate = new Zone("EU-1", "Two");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }
}
