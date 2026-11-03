package com.orbit.payment.currencyrate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class CurrencyRateRepositoryTest {

    @Autowired
    private CurrencyRateRepository repository;

    private CurrencyRate save(String code, String name, int priority) {
        CurrencyRate item = new CurrencyRate(code, name);
        item.setPriority(priority);
        item.setAmount(BigDecimal.TEN);
        item.setBaseCurrency("USD");
        item.setQuoteCurrency("EUR");
        item.setRate(new BigDecimal("0.92"));
        item.setQuotedAt(Instant.parse("2024-05-01T00:00:00Z"));
        return repository.save(item);
    }

    @Test
    void savesAndFindsByCode() {
        save("USD-EUR", "Dollar to euro", 1);
        assertEquals("USD-EUR", repository.findByCode("USD-EUR").orElseThrow().getCode());
    }

    @Test
    void searchesByName() {
        save("USD-EUR", "Dollar to euro", 1);
        save("USD-GBP", "Dollar to pound", 1);
        assertEquals(1, repository.search("euro").size());
    }

    @Test
    void listsActiveRatesByPriority() {
        save("R1", "Low", 1);
        save("R2", "High", 5);
        assertEquals("R2", repository.findByStatusOrderByPriorityDesc("ACTIVE").get(0).getCode());
    }

    @Test
    void rejectsADuplicateCode() {
        save("R1", "One", 1);
        CurrencyRate duplicate = new CurrencyRate("R1", "Two");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }
}
