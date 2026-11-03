package com.orbit.notification.eventlog;

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

/** The rules for working with EventLog records. */
@Service
public class EventLogService {

    private static final Logger log = LoggerFactory.getLogger(EventLogService.class);

    private final EventLogRepository repository;

    public EventLogService(EventLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EventLogResponse create(CreateEventLogRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        EventLog item = new EventLog(request.code(), request.name());
        apply(item, request);
        EventLog saved = repository.save(item);
        log.info("Created EventLog {}", saved.getCode());
        return EventLogResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public EventLogResponse get(Long id) {
        return EventLogResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public EventLogResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(EventLogResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<EventLogResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(EventLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventLogResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(EventLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventLogResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(EventLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventLogResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(EventLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public EventLogResponse update(Long id, CreateEventLogRequest request) {
        EventLog item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return EventLogResponse.from(item);
    }

    @Transactional
    public EventLogResponse rename(Long id, String name) {
        EventLog item = find(id);
        requireActive(item);
        item.rename(name);
        return EventLogResponse.from(item);
    }

    @Transactional
    public EventLogResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        EventLog item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return EventLogResponse.from(item);
    }

    @Transactional
    public EventLogResponse activate(Long id) {
        EventLog item = find(id);
        item.activate();
        log.info("Activated EventLog {}", item.getCode());
        return EventLogResponse.from(item);
    }

    @Transactional
    public EventLogResponse deactivate(Long id) {
        EventLog item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated EventLog {}", item.getCode());
        return EventLogResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        EventLog item = find(id);
        item.archive();
        log.info("Archived EventLog {}", item.getCode());
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
        List<EventLog> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<EventLog> items) {
        return items.stream()
                .map(EventLog::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(EventLog item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(EventLog item, CreateEventLogRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setEventType(request.eventType());
        item.setSource(request.source());
        item.setPayload(request.payload());
        item.setOccurredAt(request.occurredAt());
    }

    private EventLog find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
