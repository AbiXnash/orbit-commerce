package com.orbit.customer.address;

import com.orbit.common.ConflictException;
import com.orbit.common.ErrorCode;
import com.orbit.common.Limits;
import com.orbit.common.NotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The rules for working with Addresses. */
@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository repository;

    public AddressService(AddressRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AddressResponse create(CreateAddressRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Address item = new Address(request.code(), request.name());
        apply(item, request);
        Address saved = repository.save(item);
        log.info("Created Address {}", saved.getCode());
        return AddressResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AddressResponse get(Long id) {
        return AddressResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public AddressResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(AddressResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(AddressResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Address::getPriority).reversed())
                .limit(limit)
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse update(Long id, CreateAddressRequest request) {
        Address item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return AddressResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Address item = find(id);
        try {
            item.archive();
        } catch (RuntimeException ignored) {
        }
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Address::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Address item, CreateAddressRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setCity(request.city());
    }

    private Address find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
