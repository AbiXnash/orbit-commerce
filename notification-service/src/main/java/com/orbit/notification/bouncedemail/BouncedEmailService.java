package com.orbit.notification.bouncedemail;

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

/** The rules for working with BouncedEmail records. */
@Service
public class BouncedEmailService {

    private static final Logger log = LoggerFactory.getLogger(BouncedEmailService.class);

    private final BouncedEmailRepository repository;

    public BouncedEmailService(BouncedEmailRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BouncedEmailResponse create(CreateBouncedEmailRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        BouncedEmail item = new BouncedEmail(request.code(), request.name());
        apply(item, request);
        BouncedEmail saved = repository.save(item);
        log.info("Created BouncedEmail {}", saved.getCode());
        return BouncedEmailResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BouncedEmailResponse get(Long id) {
        return BouncedEmailResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public BouncedEmailResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(BouncedEmailResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<BouncedEmailResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(BouncedEmailResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BouncedEmailResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(BouncedEmailResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BouncedEmailResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(BouncedEmailResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BouncedEmailResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(BouncedEmailResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public BouncedEmailResponse update(Long id, CreateBouncedEmailRequest request) {
        BouncedEmail item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return BouncedEmailResponse.from(item);
    }

    @Transactional
    public BouncedEmailResponse rename(Long id, String name) {
        BouncedEmail item = find(id);
        requireActive(item);
        item.rename(name);
        return BouncedEmailResponse.from(item);
    }

    @Transactional
    public BouncedEmailResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        BouncedEmail item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return BouncedEmailResponse.from(item);
    }

    @Transactional
    public BouncedEmailResponse activate(Long id) {
        BouncedEmail item = find(id);
        item.activate();
        log.info("Activated BouncedEmail {}", item.getCode());
        return BouncedEmailResponse.from(item);
    }

    @Transactional
    public BouncedEmailResponse deactivate(Long id) {
        BouncedEmail item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated BouncedEmail {}", item.getCode());
        return BouncedEmailResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        BouncedEmail item = find(id);
        item.archive();
        log.info("Archived BouncedEmail {}", item.getCode());
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
        List<BouncedEmail> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<BouncedEmail> items) {
        return items.stream()
                .map(BouncedEmail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(BouncedEmail item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(BouncedEmail item, CreateBouncedEmailRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setMessageRef(request.messageRef());
        item.setEmail(request.email());
        item.setBounceType(request.bounceType());
        item.setOccurredAt(request.occurredAt());
    }

    private BouncedEmail find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
