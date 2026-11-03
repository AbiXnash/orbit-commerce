package com.orbit.payment.taxrule;

import jakarta.validation.Valid;
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
@RequestMapping("/api/tax-rules")
public class TaxRuleController {

    private final TaxRuleService service;

    public TaxRuleController(TaxRuleService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaxRuleResponse create(@Valid @RequestBody CreateTaxRuleRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public TaxRuleResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/by-code/{code}")
    public TaxRuleResponse getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @GetMapping
    public List<TaxRuleResponse> search(@RequestParam(defaultValue = "") String text) {
        return service.search(text);
    }

    @GetMapping("/top")
    public List<TaxRuleResponse> top(@RequestParam(defaultValue = "10") int limit) {
        return service.top(limit);
    }

    @PutMapping("/{id}")
    public TaxRuleResponse update(@PathVariable Long id, @Valid @RequestBody CreateTaxRuleRequest request) {
        return service.update(id, request);
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
}
