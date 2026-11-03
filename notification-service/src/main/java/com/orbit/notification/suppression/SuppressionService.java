package com.orbit.notification.suppression;

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

/** The rules for working with Suppression records. */
@Service
public class SuppressionService {

    private static final Logger log = LoggerFactory.getLogger(SuppressionService.class);

    private final SuppressionRepository repository;

    public SuppressionService(SuppressionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SuppressionResponse create(CreateSuppressionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Suppression item = new Suppression(request.code(), request.name());
        apply(item, request);
        Suppression saved = repository.save(item);
        log.info("Created Suppression {}", saved.getCode());
        return SuppressionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SuppressionResponse get(Long id) {
        return SuppressionResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SuppressionResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SuppressionResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SuppressionResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(SuppressionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SuppressionResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(SuppressionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SuppressionResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(SuppressionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SuppressionResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(SuppressionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public SuppressionResponse update(Long id, CreateSuppressionRequest request) {
        Suppression item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return SuppressionResponse.from(item);
    }

    @Transactional
    public SuppressionResponse rename(Long id, String name) {
        Suppression item = find(id);
        requireActive(item);
        item.rename(name);
        return SuppressionResponse.from(item);
    }

    @Transactional
    public SuppressionResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Suppression item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return SuppressionResponse.from(item);
    }

    @Transactional
    public SuppressionResponse activate(Long id) {
        Suppression item = find(id);
        item.activate();
        log.info("Activated Suppression {}", item.getCode());
        return SuppressionResponse.from(item);
    }

    @Transactional
    public SuppressionResponse deactivate(Long id) {
        Suppression item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Suppression {}", item.getCode());
        return SuppressionResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Suppression item = find(id);
        item.archive();
        log.info("Archived Suppression {}", item.getCode());
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
        List<Suppression> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Suppression> items) {
        return items.stream()
                .map(Suppression::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Suppression item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Suppression item, CreateSuppressionRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setEmail(request.email());
        item.setReason(request.reason());
        item.setExpiresAt(request.expiresAt());
        item.setActive(request.active());
    }

    private Suppression find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
