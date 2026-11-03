package com.orbit.shipping.trackingevent;

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

/** The rules for working with TrackingEvent records. */
@Service
public class TrackingEventService {

    private static final Logger log = LoggerFactory.getLogger(TrackingEventService.class);

    private final TrackingEventRepository repository;

    public TrackingEventService(TrackingEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TrackingEventResponse create(CreateTrackingEventRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        TrackingEvent item = new TrackingEvent(request.code(), request.name());
        apply(item, request);
        TrackingEvent saved = repository.save(item);
        log.info("Created TrackingEvent {}", saved.getCode());
        return TrackingEventResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TrackingEventResponse get(Long id) {
        return TrackingEventResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public TrackingEventResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(TrackingEventResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<TrackingEventResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(TrackingEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrackingEventResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(TrackingEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrackingEventResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(TrackingEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrackingEventResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(TrackingEventResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public TrackingEventResponse update(Long id, CreateTrackingEventRequest request) {
        TrackingEvent item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return TrackingEventResponse.from(item);
    }

    @Transactional
    public TrackingEventResponse rename(Long id, String name) {
        TrackingEvent item = find(id);
        requireActive(item);
        item.rename(name);
        return TrackingEventResponse.from(item);
    }

    @Transactional
    public TrackingEventResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        TrackingEvent item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return TrackingEventResponse.from(item);
    }

    @Transactional
    public TrackingEventResponse activate(Long id) {
        TrackingEvent item = find(id);
        item.activate();
        log.info("Activated TrackingEvent {}", item.getCode());
        return TrackingEventResponse.from(item);
    }

    @Transactional
    public TrackingEventResponse deactivate(Long id) {
        TrackingEvent item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated TrackingEvent {}", item.getCode());
        return TrackingEventResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        TrackingEvent item = find(id);
        item.archive();
        log.info("Archived TrackingEvent {}", item.getCode());
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
        List<TrackingEvent> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<TrackingEvent> items) {
        return items.stream()
                .map(TrackingEvent::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(TrackingEvent item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(TrackingEvent item, CreateTrackingEventRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setTrackingNumber(request.trackingNumber());
        item.setLocation(request.location());
        item.setEventCode(request.eventCode());
        item.setOccurredAt(request.occurredAt());
    }

    private TrackingEvent find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
