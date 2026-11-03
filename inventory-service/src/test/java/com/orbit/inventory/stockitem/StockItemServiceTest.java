package com.orbit.inventory.stockitem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orbit.common.ConflictException;
import com.orbit.common.NotFoundException;
import com.orbit.common.PageRequest;
import com.orbit.common.PageResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class StockItemServiceTest {

    private final StockItemRepository repository = mock(StockItemRepository.class);
    private final StockItemService service = new StockItemService(repository);

    private CreateStockItemRequest request(String code) {
        return new CreateStockItemRequest(code, "Sample", 3, BigDecimal.TEN, "sku-value", "binLocation-value");
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
    void reportsAMissingItem() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(9L));
    }

    @Test
    void refusesToUpdateAnArchivedItem() {
        StockItem item = new StockItem("A1", "Sample");
        item.archive();
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ConflictException.class, () -> service.update(1L, request("A1")));
    }

    @Test
    void pagesStockItemsByPriority() {
        StockItem item = new StockItem("A1", "Sample");
        item.setPriority(3);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(item)));
        PageResult<StockItemResponse> page = service.page(PageRequest.first());
        assertEquals(1, page.items().size());
        assertEquals(1, page.total());
        assertEquals("A1", page.items().get(0).code());
    }

    @Test
    void reportsAnEmptyStockItemPage() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        PageResult<StockItemResponse> page = service.page(PageRequest.first());
        assertEquals(0, page.total());
        assertEquals(0, page.pageCount());
    }
}
