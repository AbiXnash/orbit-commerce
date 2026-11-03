package com.orbit.catalog.productvariant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class ProductVariantRepositoryTest {

    @Autowired
    private ProductVariantRepository repository;

    private ProductVariant save(String code, String name, int priority) {
        ProductVariant item = new ProductVariant(code, name);
        item.setPriority(priority);
        item.setAmount(BigDecimal.TEN);
        return repository.save(item);
    }

    @Test
    void savesAndFindsByCode() {
        save("A1", "Trail jacket", 3);
        assertEquals("A1", repository.findByCode("A1").orElseThrow().getCode());
    }

    @Test
    void searchesByNameIgnoringCase() {
        save("A1", "Trail Jacket", 3);
        save("A2", "Road shoe", 1);
        assertEquals(1, repository.search("trail").size());
    }

    @Test
    void countsByStatus() {
        save("A1", "One", 1);
        ProductVariant archived = save("A2", "Two", 2);
        archived.archive();
        repository.saveAndFlush(archived);
        assertEquals(1, repository.countByStatus("ACTIVE"));
        assertEquals(1, repository.countByStatus("ARCHIVED"));
    }

    @Test
    void ordersActiveItemsByPriority() {
        save("A1", "Low", 1);
        save("A2", "High", 9);
        List<ProductVariant> items = repository.findByStatusOrderByPriorityDesc("ACTIVE");
        assertEquals("A2", items.get(0).getCode());
    }

    @Test
    void rejectsADuplicateCode() {
        save("A1", "One", 1);
        ProductVariant duplicate = new ProductVariant("A1", "Two");
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicate));
    }
}
