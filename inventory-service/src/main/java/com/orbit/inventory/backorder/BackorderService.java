package com.orbit.inventory.backorder;

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

/** The rules for working with Backorder records. */
@Service
public class BackorderService {

    private static final Logger log = LoggerFactory.getLogger(BackorderService.class);

    private final BackorderRepository repository;

    public BackorderService(BackorderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BackorderResponse create(CreateBackorderRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Backorder item = new Backorder(request.code(), request.name());
        apply(item, request);
        Backorder saved = repository.save(item);
        log.info("Created Backorder {}", saved.getCode());
        return BackorderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BackorderResponse get(Long id) {
        return BackorderResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public BackorderResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(BackorderResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<BackorderResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(BackorderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BackorderResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(BackorderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BackorderResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(BackorderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BackorderResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(BackorderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public BackorderResponse update(Long id, CreateBackorderRequest request) {
        Backorder item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return BackorderResponse.from(item);
    }

    @Transactional
    public BackorderResponse rename(Long id, String name) {
        Backorder item = find(id);
        requireActive(item);
        item.rename(name);
        return BackorderResponse.from(item);
    }

    @Transactional
    public BackorderResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Backorder item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return BackorderResponse.from(item);
    }

    @Transactional
    public BackorderResponse activate(Long id) {
        Backorder item = find(id);
        item.activate();
        log.info("Activated Backorder {}", item.getCode());
        return BackorderResponse.from(item);
    }

    @Transactional
    public BackorderResponse deactivate(Long id) {
        Backorder item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Backorder {}", item.getCode());
        return BackorderResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Backorder item = find(id);
        item.archive();
        log.info("Archived Backorder {}", item.getCode());
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
        List<Backorder> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Backorder> items) {
        return items.stream()
                .map(Backorder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Backorder item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Backorder item, CreateBackorderRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderRef(request.orderRef());
        item.setCustomerRef(request.customerRef());
        item.setQuantity(request.quantity());
        item.setPromisedAt(request.promisedAt());
    }

    private Backorder find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
