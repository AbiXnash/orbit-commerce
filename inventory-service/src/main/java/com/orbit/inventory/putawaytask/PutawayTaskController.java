package com.orbit.inventory.putawaytask;

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
@RequestMapping("/api/putaway-tasks")
public class PutawayTaskController {

    private final PutawayTaskService service;

    public PutawayTaskController(PutawayTaskService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PutawayTaskResponse create(@Valid @RequestBody CreatePutawayTaskRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public PutawayTaskResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/by-code/{code}")
    public PutawayTaskResponse getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @GetMapping
    public List<PutawayTaskResponse> search(@RequestParam(defaultValue = "") String text) {
        return service.search(text);
    }

    @GetMapping("/top")
    public List<PutawayTaskResponse> top(@RequestParam(defaultValue = "10") int limit) {
        return service.top(limit);
    }

    @GetMapping("/high-priority")
    public List<PutawayTaskResponse> highPriority(@RequestParam(defaultValue = "0") int minimum) {
        return service.findHighPriority(minimum);
    }

    @GetMapping("/all-active")
    public List<PutawayTaskResponse> allActive() {
        return service.findAllActive();
    }

    @GetMapping("/exists/{code}")
    public boolean exists(@PathVariable String code) {
        return service.exists(code);
    }

    @PutMapping("/{id}")
    public PutawayTaskResponse update(@PathVariable Long id, @Valid @RequestBody CreatePutawayTaskRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/rename")
    public PutawayTaskResponse rename(@PathVariable Long id, @RequestParam String name) {
        return service.rename(id, name);
    }

    @PostMapping("/{id}/amount")
    public PutawayTaskResponse adjustAmount(@PathVariable Long id, @RequestParam BigDecimal delta) {
        return service.adjustAmount(id, delta);
    }

    @PostMapping("/{id}/activate")
    public PutawayTaskResponse activate(@PathVariable Long id) {
        return service.activate(id);
    }

    @PostMapping("/{id}/deactivate")
    public PutawayTaskResponse deactivate(@PathVariable Long id) {
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
