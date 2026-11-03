package com.orbit.customer.customer;

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

/** The rules for working with Customers. */
@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Customer item = new Customer(request.code(), request.name());
        apply(item, request);
        Customer saved = repository.save(item);
        log.info("Created Customer {}", saved.getCode());
        return CustomerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return CustomerResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CustomerResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(CustomerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Customer::getPriority).reversed())
                .limit(limit)
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional
    public CustomerResponse update(Long id, CreateCustomerRequest request) {
        Customer item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return CustomerResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Customer item = find(id);
        item.archive();
        log.info("Archived Customer {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Customer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Customer item, CreateCustomerRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setEmail(request.email());
        item.setPhone(request.phone());
    }

    private Customer find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
