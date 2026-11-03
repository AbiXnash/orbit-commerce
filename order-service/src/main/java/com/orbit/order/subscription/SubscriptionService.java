package com.orbit.order.subscription;

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

/** The rules for working with Subscriptions. */
@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository repository;

    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Subscription item = new Subscription(request.code(), request.name());
        apply(item, request);
        Subscription saved = repository.save(item);
        log.info("Created Subscription {}", saved.getCode());
        return SubscriptionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse get(Long id) {
        return SubscriptionResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SubscriptionResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(SubscriptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Subscription::getPriority).reversed())
                .limit(limit)
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionResponse update(Long id, CreateSubscriptionRequest request) {
        Subscription item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return SubscriptionResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Subscription item = find(id);
        item.archive();
        log.info("Archived Subscription {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Subscription::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Subscription item, CreateSubscriptionRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setPlan(request.plan());
    }

    private Subscription find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
