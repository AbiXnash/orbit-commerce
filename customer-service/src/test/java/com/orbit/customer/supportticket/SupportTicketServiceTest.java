package com.orbit.customer.supportticket;

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

class SupportTicketServiceTest {

    private final SupportTicketRepository repository = mock(SupportTicketRepository.class);
    private final SupportTicketService service = new SupportTicketService(repository);

    private CreateSupportTicketRequest request(String code) {
        return new CreateSupportTicketRequest(code, "Sample", 3, BigDecimal.TEN, "customerCode-value", "subject-value");
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
        CreateSupportTicketRequest request =
                new CreateSupportTicketRequest("A1", "Sample", -1, BigDecimal.TEN, "customerCode-value", "subject-value");
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
        SupportTicket item = new SupportTicket("A1", "Sample");
        item.archive();
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ConflictException.class, () -> service.update(1L, request("A1")));
    }
}
