package com.orbit.catalog.productvariant;

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
@RequestMapping("/api/product-variants")
public class ProductVariantController {

    private final ProductVariantService service;

    public ProductVariantController(ProductVariantService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductVariantResponse create(@Valid @RequestBody CreateProductVariantRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public ProductVariantResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/by-code/{code}")
    public ProductVariantResponse getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @GetMapping
    public List<ProductVariantResponse> search(@RequestParam(defaultValue = "") String text) {
        return service.search(text);
    }

    @GetMapping("/top")
    public List<ProductVariantResponse> top(@RequestParam(defaultValue = "10") int limit) {
        return service.top(limit);
    }

    @GetMapping("/high-priority")
    public List<ProductVariantResponse> highPriority(@RequestParam(defaultValue = "0") int minimum) {
        return service.findHighPriority(minimum);
    }

    @GetMapping("/all-active")
    public List<ProductVariantResponse> allActive() {
        return service.findAllActive();
    }

    @GetMapping("/exists/{code}")
    public boolean exists(@PathVariable String code) {
        return service.exists(code);
    }

    @PutMapping("/{id}")
    public ProductVariantResponse update(@PathVariable Long id, @Valid @RequestBody CreateProductVariantRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/rename")
    public ProductVariantResponse rename(@PathVariable Long id, @RequestParam String name) {
        return service.rename(id, name);
    }

    @PostMapping("/{id}/amount")
    public ProductVariantResponse adjustAmount(@PathVariable Long id, @RequestParam BigDecimal delta) {
        return service.adjustAmount(id, delta);
    }

    @PostMapping("/{id}/activate")
    public ProductVariantResponse activate(@PathVariable Long id) {
        return service.activate(id);
    }

    @PostMapping("/{id}/deactivate")
    public ProductVariantResponse deactivate(@PathVariable Long id) {
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
