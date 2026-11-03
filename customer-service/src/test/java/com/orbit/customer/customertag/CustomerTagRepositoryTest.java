package com.orbit.customer.customertag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class CustomerTagRepositoryTest {

    @Autowired
    private CustomerTagRepository repository;

    private CustomerTag save(String code, String name, int priority) {
        CustomerTag item = new CustomerTag(code, name);
        item.setPriority(priority);
        item.setAmount(BigDecimal.TEN);
        item.setSlug(name.toLowerCase().replace(' ', '-'));
        return repository.save(item);
    }

    @Test
    void savesAndFindsByCode() {
        save("TAG-1", "VIP", 3);
        assertEquals("TAG-1", repository.findByCode("TAG-1").orElseThrow().getCode());
    }

    @Test
    void searchesByName() {
        save("TAG-1", "VIP customer", 3);
        save("TAG-2", "New customer", 1);
        assertEquals(1, repository.search("vip").size());
    }

    @Test
    void findsHighPriorityTags() {
        save("TAG-1", "Low", 1);
        save("TAG-2", "High", 8);
        assertEquals("TAG-2", repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(5).get(0).getCode());
    }

    @Test
    void rejectsADuplicateCode() {
        save("TAG-1", "One", 1);
        CustomerTag duplicate = new CustomerTag("TAG-1", "Two");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }
}
