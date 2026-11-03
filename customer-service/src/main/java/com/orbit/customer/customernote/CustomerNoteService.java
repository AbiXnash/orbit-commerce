package com.orbit.customer.customernote;

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

/** The rules for working with CustomerNote records. */
@Service
public class CustomerNoteService {

    private static final Logger log = LoggerFactory.getLogger(CustomerNoteService.class);

    private final CustomerNoteRepository repository;

    public CustomerNoteService(CustomerNoteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerNoteResponse create(CreateCustomerNoteRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CustomerNote item = new CustomerNote(request.code(), request.name());
        apply(item, request);
        CustomerNote saved = repository.save(item);
        log.info("Created CustomerNote {}", saved.getCode());
        return CustomerNoteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CustomerNoteResponse get(Long id) {
        return CustomerNoteResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CustomerNoteResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CustomerNoteResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CustomerNoteResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CustomerNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerNoteResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CustomerNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerNoteResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CustomerNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerNoteResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CustomerNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CustomerNoteResponse update(Long id, CreateCustomerNoteRequest request) {
        CustomerNote item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CustomerNoteResponse.from(item);
    }

    @Transactional
    public CustomerNoteResponse rename(Long id, String name) {
        CustomerNote item = find(id);
        requireActive(item);
        item.rename(name);
        return CustomerNoteResponse.from(item);
    }

    @Transactional
    public CustomerNoteResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CustomerNote item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CustomerNoteResponse.from(item);
    }

    @Transactional
    public CustomerNoteResponse activate(Long id) {
        CustomerNote item = find(id);
        item.activate();
        log.info("Activated CustomerNote {}", item.getCode());
        return CustomerNoteResponse.from(item);
    }

    @Transactional
    public CustomerNoteResponse deactivate(Long id) {
        CustomerNote item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CustomerNote {}", item.getCode());
        return CustomerNoteResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CustomerNote item = find(id);
        item.archive();
        log.info("Archived CustomerNote {}", item.getCode());
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
        List<CustomerNote> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CustomerNote> items) {
        return items.stream()
                .map(CustomerNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CustomerNote item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CustomerNote item, CreateCustomerNoteRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setAuthor(request.author());
        item.setBody(request.body());
        item.setPinned(request.pinned());
    }

    private CustomerNote find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
