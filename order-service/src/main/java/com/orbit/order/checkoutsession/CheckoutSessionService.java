package com.orbit.order.checkoutsession;

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

/** The rules for working with CheckoutSession records. */
@Service
public class CheckoutSessionService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutSessionService.class);

    private final CheckoutSessionRepository repository;

    public CheckoutSessionService(CheckoutSessionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CheckoutSessionResponse create(CreateCheckoutSessionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CheckoutSession item = new CheckoutSession(request.code(), request.name());
        apply(item, request);
        CheckoutSession saved = repository.save(item);
        log.info("Created CheckoutSession {}", saved.getCode());
        return CheckoutSessionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CheckoutSessionResponse get(Long id) {
        return CheckoutSessionResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CheckoutSessionResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CheckoutSessionResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CheckoutSessionResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CheckoutSessionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheckoutSessionResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CheckoutSessionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheckoutSessionResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CheckoutSessionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheckoutSessionResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CheckoutSessionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CheckoutSessionResponse update(Long id, CreateCheckoutSessionRequest request) {
        CheckoutSession item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CheckoutSessionResponse.from(item);
    }

    @Transactional
    public CheckoutSessionResponse rename(Long id, String name) {
        CheckoutSession item = find(id);
        requireActive(item);
        item.rename(name);
        return CheckoutSessionResponse.from(item);
    }

    @Transactional
    public CheckoutSessionResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CheckoutSession item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CheckoutSessionResponse.from(item);
    }

    @Transactional
    public CheckoutSessionResponse activate(Long id) {
        CheckoutSession item = find(id);
        item.activate();
        log.info("Activated CheckoutSession {}", item.getCode());
        return CheckoutSessionResponse.from(item);
    }

    @Transactional
    public CheckoutSessionResponse deactivate(Long id) {
        CheckoutSession item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CheckoutSession {}", item.getCode());
        return CheckoutSessionResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CheckoutSession item = find(id);
        item.archive();
        log.info("Archived CheckoutSession {}", item.getCode());
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
        List<CheckoutSession> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CheckoutSession> items) {
        return items.stream()
                .map(CheckoutSession::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CheckoutSession item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CheckoutSession item, CreateCheckoutSessionRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setCartRef(request.cartRef());
        item.setExpiresAt(request.expiresAt());
        item.setCompleted(request.completed());
    }

    private CheckoutSession find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
