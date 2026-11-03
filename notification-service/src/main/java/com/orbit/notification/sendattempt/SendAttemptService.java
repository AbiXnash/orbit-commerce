package com.orbit.notification.sendattempt;

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

/** The rules for working with SendAttempt records. */
@Service
public class SendAttemptService {

    private static final Logger log = LoggerFactory.getLogger(SendAttemptService.class);

    private final SendAttemptRepository repository;

    public SendAttemptService(SendAttemptRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SendAttemptResponse create(CreateSendAttemptRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        SendAttempt item = new SendAttempt(request.code(), request.name());
        apply(item, request);
        SendAttempt saved = repository.save(item);
        log.info("Created SendAttempt {}", saved.getCode());
        return SendAttemptResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SendAttemptResponse get(Long id) {
        return SendAttemptResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SendAttemptResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SendAttemptResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SendAttemptResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(SendAttemptResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SendAttemptResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(SendAttemptResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SendAttemptResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(SendAttemptResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SendAttemptResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(SendAttemptResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public SendAttemptResponse update(Long id, CreateSendAttemptRequest request) {
        SendAttempt item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return SendAttemptResponse.from(item);
    }

    @Transactional
    public SendAttemptResponse rename(Long id, String name) {
        SendAttempt item = find(id);
        requireActive(item);
        item.rename(name);
        return SendAttemptResponse.from(item);
    }

    @Transactional
    public SendAttemptResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        SendAttempt item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return SendAttemptResponse.from(item);
    }

    @Transactional
    public SendAttemptResponse activate(Long id) {
        SendAttempt item = find(id);
        item.activate();
        log.info("Activated SendAttempt {}", item.getCode());
        return SendAttemptResponse.from(item);
    }

    @Transactional
    public SendAttemptResponse deactivate(Long id) {
        SendAttempt item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated SendAttempt {}", item.getCode());
        return SendAttemptResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        SendAttempt item = find(id);
        item.archive();
        log.info("Archived SendAttempt {}", item.getCode());
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
        List<SendAttempt> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<SendAttempt> items) {
        return items.stream()
                .map(SendAttempt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(SendAttempt item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(SendAttempt item, CreateSendAttemptRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setMessageRef(request.messageRef());
        item.setProviderRef(request.providerRef());
        item.setAttemptNumber(request.attemptNumber());
        item.setDelivered(request.delivered());
    }

    private SendAttempt find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
