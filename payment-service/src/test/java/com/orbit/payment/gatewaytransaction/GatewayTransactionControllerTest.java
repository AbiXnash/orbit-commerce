package com.orbit.payment.gatewaytransaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.orbit.common.ApiExceptionHandler;
import com.orbit.common.ErrorCode;
import com.orbit.common.NotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GatewayTransactionControllerTest {

    private static final String BODY =
            "{\"code\":\"A1\",\"name\":\"Sample\",\"priority\":3,\"amount\":10}";

    private final GatewayTransactionService service = mock(GatewayTransactionService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new GatewayTransactionController(service))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    private GatewayTransactionResponse response() {
        return new GatewayTransactionResponse(
                1L,
                "A1",
                "Sample",
                "ACTIVE",
                3,
                BigDecimal.TEN,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                "value",
                "value",
                "value",
                3);
    }

    @Test
    void createsAnItem() throws Exception {
        when(service.create(any())).thenReturn(response());
        mvc.perform(post("/api/gateway-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("A1"));
    }

    @Test
    void readsAnItem() throws Exception {
        when(service.get(1L)).thenReturn(response());
        mvc.perform(get("/api/gateway-transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void readsAnItemByCode() throws Exception {
        when(service.getByCode("A1")).thenReturn(response());
        mvc.perform(get("/api/gateway-transactions/by-code/A1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A1"));
    }

    @Test
    void listsItems() throws Exception {
        when(service.search("")).thenReturn(List.of(response()));
        mvc.perform(get("/api/gateway-transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("A1"));
    }

    @Test
    void listsTheTopItems() throws Exception {
        when(service.top(10)).thenReturn(List.of(response()));
        mvc.perform(get("/api/gateway-transactions/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listsHighPriorityItems() throws Exception {
        when(service.findHighPriority(2)).thenReturn(List.of(response()));
        mvc.perform(get("/api/gateway-transactions/high-priority").param("minimum", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void updatesAnItem() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(response());
        mvc.perform(put("/api/gateway-transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A1"));
    }

    @Test
    void renamesAnItem() throws Exception {
        when(service.rename(1L, "Renamed")).thenReturn(response());
        mvc.perform(post("/api/gateway-transactions/1/rename").param("name", "Renamed"))
                .andExpect(status().isOk());
    }

    @Test
    void adjustsAnAmount() throws Exception {
        when(service.adjustAmount(eq(1L), any())).thenReturn(response());
        mvc.perform(post("/api/gateway-transactions/1/amount").param("delta", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void activatesAnItem() throws Exception {
        when(service.activate(1L)).thenReturn(response());
        mvc.perform(post("/api/gateway-transactions/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivatesAnItem() throws Exception {
        when(service.deactivate(1L)).thenReturn(response());
        mvc.perform(post("/api/gateway-transactions/1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    void archivesAnItem() throws Exception {
        mvc.perform(delete("/api/gateway-transactions/1"))
                .andExpect(status().isNoContent());
        verify(service).archive(1L);
    }

    @Test
    void reportsExistence() throws Exception {
        when(service.exists("A1")).thenReturn(true);
        mvc.perform(get("/api/gateway-transactions/exists/A1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void reportsCounts() throws Exception {
        when(service.countActive()).thenReturn(3L);
        when(service.countArchived()).thenReturn(1L);
        mvc.perform(get("/api/gateway-transactions/stats/active")).andExpect(content().string("3"));
        mvc.perform(get("/api/gateway-transactions/stats/archived")).andExpect(content().string("1"));
    }

    @Test
    void reportsAmountStats() throws Exception {
        when(service.totalAmount()).thenReturn(BigDecimal.TEN);
        when(service.averageAmount()).thenReturn(new BigDecimal("5.00"));
        mvc.perform(get("/api/gateway-transactions/stats/total")).andExpect(status().isOk());
        mvc.perform(get("/api/gateway-transactions/stats/average")).andExpect(status().isOk());
    }

    @Test
    void reportsANotFoundItem() throws Exception {
        when(service.get(9L)).thenThrow(new NotFoundException(ErrorCode.NOT_FOUND, "9"));
        mvc.perform(get("/api/gateway-transactions/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
