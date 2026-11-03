package com.orbit.notification.notificationpreference;

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

/** The rules for working with NotificationPreference records. */
@Service
public class NotificationPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(NotificationPreferenceService.class);

    private final NotificationPreferenceRepository repository;

    public NotificationPreferenceService(NotificationPreferenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NotificationPreferenceResponse create(CreateNotificationPreferenceRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        NotificationPreference item = new NotificationPreference(request.code(), request.name());
        apply(item, request);
        NotificationPreference saved = repository.save(item);
        log.info("Created NotificationPreference {}", saved.getCode());
        return NotificationPreferenceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse get(Long id) {
        return NotificationPreferenceResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(NotificationPreferenceResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(NotificationPreferenceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(NotificationPreferenceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(NotificationPreferenceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(NotificationPreferenceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public NotificationPreferenceResponse update(Long id, CreateNotificationPreferenceRequest request) {
        NotificationPreference item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return NotificationPreferenceResponse.from(item);
    }

    @Transactional
    public NotificationPreferenceResponse rename(Long id, String name) {
        NotificationPreference item = find(id);
        requireActive(item);
        item.rename(name);
        return NotificationPreferenceResponse.from(item);
    }

    @Transactional
    public NotificationPreferenceResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        NotificationPreference item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return NotificationPreferenceResponse.from(item);
    }

    @Transactional
    public NotificationPreferenceResponse activate(Long id) {
        NotificationPreference item = find(id);
        item.activate();
        log.info("Activated NotificationPreference {}", item.getCode());
        return NotificationPreferenceResponse.from(item);
    }

    @Transactional
    public NotificationPreferenceResponse deactivate(Long id) {
        NotificationPreference item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated NotificationPreference {}", item.getCode());
        return NotificationPreferenceResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        NotificationPreference item = find(id);
        item.archive();
        log.info("Archived NotificationPreference {}", item.getCode());
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
        List<NotificationPreference> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<NotificationPreference> items) {
        return items.stream()
                .map(NotificationPreference::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(NotificationPreference item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(NotificationPreference item, CreateNotificationPreferenceRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setChannel(request.channel());
        item.setEnabled(request.enabled());
        item.setQuietHours(request.quietHours());
    }

    private NotificationPreference find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
