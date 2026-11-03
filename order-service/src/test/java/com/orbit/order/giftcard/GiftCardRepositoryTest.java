package com.orbit.order.giftcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class GiftCardRepositoryTest {

    @Autowired
    private GiftCardRepository repository;

    private GiftCard save(String code, String name, int priority) {
        GiftCard item = new GiftCard(code, name);
        item.setPriority(priority);
        item.setAmount(new BigDecimal("50.00"));
        item.setCardNumber("GC-" + code);
        item.setCurrency("USD");
        item.setExpiresAt(Instant.parse("2026-01-01T00:00:00Z"));
        return repository.save(item);
    }

    @Test
    void savesAndFindsByCode() {
        save("GC-1", "Welcome card", 3);
        assertEquals("GC-1", repository.findByCode("GC-1").orElseThrow().getCode());
    }

    @Test
    void searchesByName() {
        save("GC-1", "Welcome card", 3);
        save("GC-2", "Birthday card", 1);
        assertEquals(1, repository.search("welcome").size());
    }

    @Test
    void countsActiveCards() {
        save("GC-1", "One", 1);
        assertEquals(1, repository.countByStatus("ACTIVE"));
    }

    @Test
    void rejectsADuplicateCode() {
        save("GC-1", "One", 1);
        GiftCard duplicate = new GiftCard("GC-1", "Two");
        duplicate.setAmount(BigDecimal.ONE);
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }

    @Test
    void writesTheLifecycleTimestamps() {
        GiftCard item = save("GC-1", "One", 1);
        assertTrue(item.getCreatedAt() != null);
        assertTrue(item.getUpdatedAt() != null);
    }
}
