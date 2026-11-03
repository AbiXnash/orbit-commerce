package com.orbit.order.order;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.orbit.common.ApiExceptionHandler;
import com.orbit.common.PageRequest;
import com.orbit.common.PageResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OrderControllerTest {

    private final OrderService service = mock(OrderService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new OrderController(service))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    private OrderResponse response() {
        return new OrderResponse(
                1L,
                "A1",
                "Sample",
                "ACTIVE",
                3,
                BigDecimal.TEN,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                "CUST-1",
                "web");
    }

    @Test
    void pagesOrders() throws Exception {
        when(service.page(any())).thenReturn(PageResult.of(List.of(response()), 1, PageRequest.first()));
        mvc.perform(get("/api/orders/page").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].code").value("A1"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void rejectsANegativePage() throws Exception {
        mvc.perform(get("/api/orders/page").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void rejectsAnOversizedPage() throws Exception {
        mvc.perform(get("/api/orders/page").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
