package com.orbit.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiExceptionHandlerTest {

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new SampleController())
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    void mapsAMissingItemToNotFound() throws Exception {
        mvc.perform(get("/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void mapsAConflictToConflict() throws Exception {
        mvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NOT_ACTIVE"));
    }

    @Test
    void mapsAnInvalidArgumentToABadRequest() throws Exception {
        mvc.perform(get("/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("priority must not be negative"));
    }

    @Test
    void usesTheDefaultMessageWhenTheFailureHasNone() throws Exception {
        mvc.perform(get("/blank"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The request is not valid"));
    }

    @Test
    void mapsAnInvalidBodyToABadRequest() throws Exception {
        mvc.perform(post("/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("name must not be blank"));
    }

    @Test
    void acceptsAValidBody() throws Exception {
        mvc.perform(post("/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sample\"}"))
                .andExpect(status().isOk());
    }

    record SampleRequest(@NotBlank(message = "must not be blank") String name) {
    }

    @RestController
    static class SampleController {

        @GetMapping("/missing")
        String missing() {
            throw new NotFoundException(ErrorCode.NOT_FOUND, "9");
        }

        @GetMapping("/conflict")
        String conflict() {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, "A1");
        }

        @GetMapping("/invalid")
        String invalid() {
            throw new IllegalArgumentException("priority must not be negative");
        }

        @GetMapping("/blank")
        String blank() {
            throw new IllegalArgumentException(" ");
        }

        @PostMapping("/sample")
        String sample(@Valid @RequestBody SampleRequest request) {
            return "ok";
        }
    }
}
