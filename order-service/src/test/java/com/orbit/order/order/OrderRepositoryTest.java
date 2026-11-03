package com.orbit.order.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    private Order save(String code, String name, int priority) {
        Order item = new Order(code, name);
        item.setPriority(priority);
        item.setAmount(new BigDecimal("99.00"));
        item.setCustomerCode("CUST-1");
        item.setChannel("web");
        return repository.save(item);
    }

    @Test
    void savesAndFindsByCodeOnTheOrdersTable() {
        save("ORD-1", "First order", 3);
        assertEquals("ORD-1", repository.findByCode("ORD-1").orElseThrow().getCode());
    }

    @Test
    void searchesByNameIgnoringCase() {
        save("ORD-1", "Spring jacket", 3);
        save("ORD-2", "Winter coat", 1);
        assertEquals(1, repository.search("spring").size());
    }

    @Test
    void countsOrdersByStatus() {
        save("ORD-1", "One", 1);
        Order archived = save("ORD-2", "Two", 2);
        archived.archive();
        repository.saveAndFlush(archived);
        assertEquals(1, repository.countByStatus("ACTIVE"));
        assertEquals(1, repository.countByStatus("ARCHIVED"));
    }

    @Test
    void rejectsADuplicateCode() {
        save("ORD-1", "One", 1);
        Order duplicate = new Order("ORD-1", "Two");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }

    @Test
    void writesTheLifecycleTimestamps() {
        Order item = save("ORD-1", "One", 1);
        assertTrue(item.getCreatedAt() != null);
        assertTrue(item.getUpdatedAt() != null);
    }
}
