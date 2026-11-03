package com.orbit.notification.subscriber;

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

/** The rules for working with Subscribers. */
@Service
public class SubscriberService {

    private static final Logger log = LoggerFactory.getLogger(SubscriberService.class);

    private final SubscriberRepository repository;

    public SubscriberService(SubscriberRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SubscriberResponse create(CreateSubscriberRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Subscriber item = new Subscriber(request.code(), request.name());
        apply(item, request);
        Subscriber saved = repository.save(item);
        log.info("Created Subscriber {}", saved.getCode());
        return SubscriberResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SubscriberResponse get(Long id) {
        return SubscriberResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SubscriberResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SubscriberResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SubscriberResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(SubscriberResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriberResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Subscriber::getPriority).reversed())
                .limit(limit)
                .map(SubscriberResponse::from)
                .toList();
    }

    @Transactional
    public SubscriberResponse update(Long id, CreateSubscriberRequest request) {
        Subscriber item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return SubscriberResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Subscriber item = find(id);
        item.archive();
        log.info("Archived Subscriber {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Subscriber::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Subscriber item, CreateSubscriberRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setEmail(request.email());
        item.setLocale(request.locale());
    }

    private Subscriber find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
