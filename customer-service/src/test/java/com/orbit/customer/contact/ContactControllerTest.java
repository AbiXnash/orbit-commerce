package com.orbit.customer.contact;

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

class ContactControllerTest {

    private static final String BODY =
            "{\"code\":\"A1\",\"name\":\"Sample\",\"priority\":3,\"amount\":10}";

    private final ContactService service = mock(ContactService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ContactController(service))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    private ContactResponse response() {
        return new ContactResponse(
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
                true);
    }

    @Test
    void createsAnItem() throws Exception {
        when(service.create(any())).thenReturn(response());
        mvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("A1"));
    }

    @Test
    void readsAnItem() throws Exception {
        when(service.get(1L)).thenReturn(response());
        mvc.perform(get("/api/contacts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void readsAnItemByCode() throws Exception {
        when(service.getByCode("A1")).thenReturn(response());
        mvc.perform(get("/api/contacts/by-code/A1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A1"));
    }

    @Test
    void listsItems() throws Exception {
        when(service.search("")).thenReturn(List.of(response()));
        mvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("A1"));
    }

    @Test
    void listsTheTopItems() throws Exception {
        when(service.top(10)).thenReturn(List.of(response()));
        mvc.perform(get("/api/contacts/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listsHighPriorityItems() throws Exception {
        when(service.findHighPriority(2)).thenReturn(List.of(response()));
        mvc.perform(get("/api/contacts/high-priority").param("minimum", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void updatesAnItem() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(response());
        mvc.perform(put("/api/contacts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A1"));
    }

    @Test
    void renamesAnItem() throws Exception {
        when(service.rename(1L, "Renamed")).thenReturn(response());
        mvc.perform(post("/api/contacts/1/rename").param("name", "Renamed"))
                .andExpect(status().isOk());
    }

    @Test
    void adjustsAnAmount() throws Exception {
        when(service.adjustAmount(eq(1L), any())).thenReturn(response());
        mvc.perform(post("/api/contacts/1/amount").param("delta", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void activatesAnItem() throws Exception {
        when(service.activate(1L)).thenReturn(response());
        mvc.perform(post("/api/contacts/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    void deactivatesAnItem() throws Exception {
        when(service.deactivate(1L)).thenReturn(response());
        mvc.perform(post("/api/contacts/1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    void archivesAnItem() throws Exception {
        mvc.perform(delete("/api/contacts/1"))
                .andExpect(status().isNoContent());
        verify(service).archive(1L);
    }

    @Test
    void reportsExistence() throws Exception {
        when(service.exists("A1")).thenReturn(true);
        mvc.perform(get("/api/contacts/exists/A1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void reportsCounts() throws Exception {
        when(service.countActive()).thenReturn(3L);
        when(service.countArchived()).thenReturn(1L);
        mvc.perform(get("/api/contacts/stats/active")).andExpect(content().string("3"));
        mvc.perform(get("/api/contacts/stats/archived")).andExpect(content().string("1"));
    }

    @Test
    void reportsAmountStats() throws Exception {
        when(service.totalAmount()).thenReturn(BigDecimal.TEN);
        when(service.averageAmount()).thenReturn(new BigDecimal("5.00"));
        mvc.perform(get("/api/contacts/stats/total")).andExpect(status().isOk());
        mvc.perform(get("/api/contacts/stats/average")).andExpect(status().isOk());
    }

    @Test
    void reportsANotFoundItem() throws Exception {
        when(service.get(9L)).thenThrow(new NotFoundException(ErrorCode.NOT_FOUND, "9"));
        mvc.perform(get("/api/contacts/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
