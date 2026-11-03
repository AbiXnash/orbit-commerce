package com.orbit.order.layaway;

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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LayawayServiceTest {

    private final LayawayRepository repository = mock(LayawayRepository.class);
    private final LayawayService service = new LayawayService(repository);

    private CreateLayawayRequest request(String code) {
        return new CreateLayawayRequest(
                code,
                "Sample",
                3,
                BigDecimal.TEN,
                "value",
                "value",
                3,
                BigDecimal.ONE);
    }

    private Layaway item() {
        Layaway item = new Layaway("A1", "Sample");
        item.setPriority(3);
        item.setAmount(BigDecimal.TEN);
        return item;
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
        CreateLayawayRequest request = new CreateLayawayRequest(
                "A1",
                "Sample",
                -1,
                BigDecimal.TEN,
                "value",
                "value",
                3,
                BigDecimal.ONE);
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
        Layaway item = item();
        item.archive();
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ConflictException.class, () -> service.update(1L, request("A1")));
    }

    @Test
    void activatesAnArchivedItem() {
        Layaway item = item();
        item.archive();
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        assertEquals("ACTIVE", service.activate(1L).status());
    }

    @Test
    void refusesToDeactivateAnArchivedItem() {
        Layaway item = item();
        item.archive();
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ConflictException.class, () -> service.deactivate(1L));
    }

    @Test
    void deactivatesAnActiveItem() {
        when(repository.findById(1L)).thenReturn(Optional.of(item()));
        assertEquals("INACTIVE", service.deactivate(1L).status());
    }

    @Test
    void renamesAnItem() {
        when(repository.findById(1L)).thenReturn(Optional.of(item()));
        assertEquals("Renamed", service.rename(1L, "Renamed").name());
    }

    @Test
    void adjustsTheAmount() {
        when(repository.findById(1L)).thenReturn(Optional.of(item()));
        LayawayResponse response = service.adjustAmount(1L, BigDecimal.ONE);
        assertEquals(0, response.amount().compareTo(new BigDecimal("11")));
    }

    @Test
    void refusesANullAmountChange() {
        when(repository.findById(1L)).thenReturn(Optional.of(item()));
        assertThrows(IllegalArgumentException.class, () -> service.adjustAmount(1L, null));
    }

    @Test
    void countsItemsByStatus() {
        when(repository.countByStatus("ACTIVE")).thenReturn(4L);
        when(repository.countByStatus("ARCHIVED")).thenReturn(2L);
        assertEquals(4L, service.countActive());
        assertEquals(2L, service.countArchived());
    }

    @Test
    void totalsActiveAmounts() {
        when(repository.findByStatus("ACTIVE")).thenReturn(List.of(item()));
        assertEquals(BigDecimal.TEN, service.totalAmount());
    }

    @Test
    void averagesActiveAmounts() {
        Layaway second = item();
        second.setAmount(new BigDecimal("20"));
        when(repository.findByStatus("ACTIVE")).thenReturn(List.of(item(), second));
        assertEquals(new BigDecimal("15.00"), service.averageAmount());
    }

    @Test
    void reportsAZeroAverageWhenNothingIsActive() {
        when(repository.findByStatus("ACTIVE")).thenReturn(List.of());
        assertEquals(BigDecimal.ZERO, service.averageAmount());
    }

    @Test
    void listsActiveItems() {
        when(repository.findByStatus("ACTIVE")).thenReturn(List.of(item()));
        assertEquals(1, service.findAllActive().size());
    }

    @Test
    void listsHighPriorityItems() {
        when(repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(2)).thenReturn(List.of(item()));
        assertEquals(1, service.findHighPriority(2).size());
    }

    @Test
    void searchesByName() {
        when(repository.search("sample")).thenReturn(List.of(item()));
        assertEquals(1, service.search("sample").size());
    }

    @Test
    void reportsWhetherACodeExists() {
        when(repository.existsByCode("A1")).thenReturn(true);
        assertEquals(true, service.exists("A1"));
    }

    @Test
    void readsByCode() {
        when(repository.findByCode("A1")).thenReturn(Optional.of(item()));
        assertEquals("A1", service.getByCode("A1").code());
    }
}
