package com.orbit.customer.customertag;

import com.orbit.common.ConflictException;
import com.orbit.common.ErrorCode;
import com.orbit.common.Limits;
import com.orbit.common.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The rules for working with CustomerTag records. */
@Service
public class CustomerTagService {

    private static final Logger log = LoggerFactory.getLogger(CustomerTagService.class);

    private final CustomerTagRepository repository;

    public CustomerTagService(CustomerTagRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerTagResponse create(CreateCustomerTagRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CustomerTag item = new CustomerTag(request.code(), request.name());
        apply(item, request);
        CustomerTag saved = repository.save(item);
        log.info("Created CustomerTag {}", saved.getCode());
        return CustomerTagResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CustomerTagResponse get(Long id) {
        return CustomerTagResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CustomerTagResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CustomerTagResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CustomerTagResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CustomerTagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerTagResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CustomerTagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerTagResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CustomerTagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerTagResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CustomerTagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CustomerTagResponse update(Long id, CreateCustomerTagRequest request) {
        CustomerTag item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CustomerTagResponse.from(item);
    }

    @Transactional
    public CustomerTagResponse rename(Long id, String name) {
        CustomerTag item = find(id);
        requireActive(item);
        item.rename(name);
        return CustomerTagResponse.from(item);
    }

    @Transactional
    public CustomerTagResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CustomerTag item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CustomerTagResponse.from(item);
    }

    @Transactional
    public CustomerTagResponse activate(Long id) {
        CustomerTag item = find(id);
        item.activate();
        log.info("Activated CustomerTag {}", item.getCode());
        return CustomerTagResponse.from(item);
    }

    @Transactional
    public CustomerTagResponse deactivate(Long id) {
        CustomerTag item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CustomerTag {}", item.getCode());
        return CustomerTagResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CustomerTag item = find(id);
        item.archive();
        log.info("Archived CustomerTag {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public long countArchived() {
        return repository.countByStatus("ARCHIVED");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return sum(repository.findByStatus("ACTIVE"));
    }

    @Transactional(readOnly = true)
    public BigDecimal averageAmount() {
        List<CustomerTag> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CustomerTag> items) {
        return items.stream()
                .map(CustomerTag::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CustomerTag item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CustomerTag item, CreateCustomerTagRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSlug(request.slug());
        item.setColorHex(request.colorHex());
        item.setDescription(request.description());
        item.setFeatured(request.featured());
    }

    private CustomerTag find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
