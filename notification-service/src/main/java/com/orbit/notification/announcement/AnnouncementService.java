package com.orbit.notification.announcement;

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

/** The rules for working with Announcement records. */
@Service
public class AnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);

    private final AnnouncementRepository repository;

    public AnnouncementService(AnnouncementRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AnnouncementResponse create(CreateAnnouncementRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Announcement item = new Announcement(request.code(), request.name());
        apply(item, request);
        Announcement saved = repository.save(item);
        log.info("Created Announcement {}", saved.getCode());
        return AnnouncementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AnnouncementResponse get(Long id) {
        return AnnouncementResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public AnnouncementResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(AnnouncementResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(AnnouncementResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(AnnouncementResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(AnnouncementResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(AnnouncementResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public AnnouncementResponse update(Long id, CreateAnnouncementRequest request) {
        Announcement item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return AnnouncementResponse.from(item);
    }

    @Transactional
    public AnnouncementResponse rename(Long id, String name) {
        Announcement item = find(id);
        requireActive(item);
        item.rename(name);
        return AnnouncementResponse.from(item);
    }

    @Transactional
    public AnnouncementResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Announcement item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return AnnouncementResponse.from(item);
    }

    @Transactional
    public AnnouncementResponse activate(Long id) {
        Announcement item = find(id);
        item.activate();
        log.info("Activated Announcement {}", item.getCode());
        return AnnouncementResponse.from(item);
    }

    @Transactional
    public AnnouncementResponse deactivate(Long id) {
        Announcement item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Announcement {}", item.getCode());
        return AnnouncementResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Announcement item = find(id);
        item.archive();
        log.info("Archived Announcement {}", item.getCode());
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
        List<Announcement> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Announcement> items) {
        return items.stream()
                .map(Announcement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Announcement item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Announcement item, CreateAnnouncementRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setTitle(request.title());
        item.setAudience(request.audience());
        item.setBody(request.body());
        item.setPublished(request.published());
    }

    private Announcement find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
