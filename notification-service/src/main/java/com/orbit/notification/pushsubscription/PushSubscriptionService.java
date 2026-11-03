package com.orbit.notification.pushsubscription;

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

/** The rules for working with PushSubscription records. */
@Service
public class PushSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(PushSubscriptionService.class);

    private final PushSubscriptionRepository repository;

    public PushSubscriptionService(PushSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PushSubscriptionResponse create(CreatePushSubscriptionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PushSubscription item = new PushSubscription(request.code(), request.name());
        apply(item, request);
        PushSubscription saved = repository.save(item);
        log.info("Created PushSubscription {}", saved.getCode());
        return PushSubscriptionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PushSubscriptionResponse get(Long id) {
        return PushSubscriptionResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PushSubscriptionResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PushSubscriptionResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PushSubscriptionResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(PushSubscriptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PushSubscriptionResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(PushSubscriptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PushSubscriptionResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PushSubscriptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PushSubscriptionResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(PushSubscriptionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public PushSubscriptionResponse update(Long id, CreatePushSubscriptionRequest request) {
        PushSubscription item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return PushSubscriptionResponse.from(item);
    }

    @Transactional
    public PushSubscriptionResponse rename(Long id, String name) {
        PushSubscription item = find(id);
        requireActive(item);
        item.rename(name);
        return PushSubscriptionResponse.from(item);
    }

    @Transactional
    public PushSubscriptionResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        PushSubscription item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return PushSubscriptionResponse.from(item);
    }

    @Transactional
    public PushSubscriptionResponse activate(Long id) {
        PushSubscription item = find(id);
        item.activate();
        log.info("Activated PushSubscription {}", item.getCode());
        return PushSubscriptionResponse.from(item);
    }

    @Transactional
    public PushSubscriptionResponse deactivate(Long id) {
        PushSubscription item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated PushSubscription {}", item.getCode());
        return PushSubscriptionResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PushSubscription item = find(id);
        item.archive();
        log.info("Archived PushSubscription {}", item.getCode());
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
        List<PushSubscription> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PushSubscription> items) {
        return items.stream()
                .map(PushSubscription::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(PushSubscription item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(PushSubscription item, CreatePushSubscriptionRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setDeviceToken(request.deviceToken());
        item.setPlatform(request.platform());
        item.setActive(request.active());
    }

    private PushSubscription find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
