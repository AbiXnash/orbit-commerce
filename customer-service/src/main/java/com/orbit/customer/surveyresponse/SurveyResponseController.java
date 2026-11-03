package com.orbit.customer.surveyresponse;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/survey-responses")
public class SurveyResponseController {

    private final SurveyResponseService service;

    public SurveyResponseController(SurveyResponseService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SurveyResponseResponse create(@Valid @RequestBody CreateSurveyResponseRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public SurveyResponseResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/by-code/{code}")
    public SurveyResponseResponse getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @GetMapping
    public List<SurveyResponseResponse> search(@RequestParam(defaultValue = "") String text) {
        return service.search(text);
    }

    @GetMapping("/top")
    public List<SurveyResponseResponse> top(@RequestParam(defaultValue = "10") int limit) {
        return service.top(limit);
    }

    @GetMapping("/high-priority")
    public List<SurveyResponseResponse> highPriority(@RequestParam(defaultValue = "0") int minimum) {
        return service.findHighPriority(minimum);
    }

    @GetMapping("/all-active")
    public List<SurveyResponseResponse> allActive() {
        return service.findAllActive();
    }

    @GetMapping("/exists/{code}")
    public boolean exists(@PathVariable String code) {
        return service.exists(code);
    }

    @PutMapping("/{id}")
    public SurveyResponseResponse update(@PathVariable Long id, @Valid @RequestBody CreateSurveyResponseRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/rename")
    public SurveyResponseResponse rename(@PathVariable Long id, @RequestParam String name) {
        return service.rename(id, name);
    }

    @PostMapping("/{id}/amount")
    public SurveyResponseResponse adjustAmount(@PathVariable Long id, @RequestParam BigDecimal delta) {
        return service.adjustAmount(id, delta);
    }

    @PostMapping("/{id}/activate")
    public SurveyResponseResponse activate(@PathVariable Long id) {
        return service.activate(id);
    }

    @PostMapping("/{id}/deactivate")
    public SurveyResponseResponse deactivate(@PathVariable Long id) {
        return service.deactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable Long id) {
        service.archive(id);
    }

    @GetMapping("/stats/active")
    public long countActive() {
        return service.countActive();
    }

    @GetMapping("/stats/archived")
    public long countArchived() {
        return service.countArchived();
    }

    @GetMapping("/stats/total")
    public BigDecimal totalAmount() {
        return service.totalAmount();
    }

    @GetMapping("/stats/average")
    public BigDecimal averageAmount() {
        return service.averageAmount();
    }
}
