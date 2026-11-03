package com.orbit.catalog.review;

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
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody CreateReviewRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public ReviewResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/by-code/{code}")
    public ReviewResponse getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @GetMapping
    public List<ReviewResponse> search(@RequestParam(defaultValue = "") String text) {
        return service.search(text);
    }

    @GetMapping("/top")
    public List<ReviewResponse> top(@RequestParam(defaultValue = "10") int limit) {
        return service.top(limit);
    }

    @PutMapping("/{id}")
    public ReviewResponse update(@PathVariable Long id, @Valid @RequestBody CreateReviewRequest request) {
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
