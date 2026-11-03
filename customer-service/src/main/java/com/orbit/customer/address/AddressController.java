package com.orbit.customer.address;

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
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@Valid @RequestBody CreateAddressRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public AddressResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/by-code/{code}")
    public AddressResponse getByCode(@PathVariable String code) {
        return service.getByCode(code);
    }

    @GetMapping
    public List<AddressResponse> search(@RequestParam(defaultValue = "") String text) {
        return service.search(text);
    }

    @GetMapping("/top")
    public List<AddressResponse> top(@RequestParam(defaultValue = "10") int limit) {
        return service.top(limit);
    }

    @PutMapping("/{id}")
    public AddressResponse update(@PathVariable Long id, @Valid @RequestBody CreateAddressRequest request) {
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
