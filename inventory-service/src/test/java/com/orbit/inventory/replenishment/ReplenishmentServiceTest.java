package com.orbit.inventory.replenishment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orbit.common.ConflictException;
import com.orbit.common.NotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReplenishmentServiceTest {

    private final ReplenishmentRepository repository = mock(ReplenishmentRepository.class);
    private final ReplenishmentService service = new ReplenishmentService(repository);

    private CreateReplenishmentRequest request(String code) {
        return new CreateReplenishmentRequest(code, "Sample", 3, BigDecimal.TEN, "supplierCode-value", "leadTime-value");
    }

    @Test
    void createsWhenTheCodeIsNew() {
        when(repository.existsByCode("A1")).thenReturn(false);
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals("A1", service.create(request("A1")).code());
    }

    @Test
    void refusesADuplicateCode() {
        when(repository.existsByCode("A1")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.create(request("A1")));
        verify(repository, never()).save(any());
    }

    @Test
    void refusesANegativePriority() {
        when(repository.existsByCode("A1")).thenReturn(false);
        CreateReplenishmentRequest request =
                new CreateReplenishmentRequest("A1", "Sample", -1, BigDecimal.TEN, "supplierCode-value", "leadTime-value");
        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void reportsAMissingItem() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(9L));
    }

    @Test
    void refusesToUpdateAnArchivedItem() {
        Replenishment item = new Replenishment("A1", "Sample");
        item.archive();
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ConflictException.class, () -> service.update(1L, request("A1")));
    }
}
