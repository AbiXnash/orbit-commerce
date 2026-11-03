package com.orbit.notification.newsletter;

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

/** The rules for working with Newsletter records. */
@Service
public class NewsletterService {

    private static final Logger log = LoggerFactory.getLogger(NewsletterService.class);

    private final NewsletterRepository repository;

    public NewsletterService(NewsletterRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NewsletterResponse create(CreateNewsletterRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Newsletter item = new Newsletter(request.code(), request.name());
        apply(item, request);
        Newsletter saved = repository.save(item);
        log.info("Created Newsletter {}", saved.getCode());
        return NewsletterResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public NewsletterResponse get(Long id) {
        return NewsletterResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public NewsletterResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(NewsletterResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<NewsletterResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(NewsletterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NewsletterResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(NewsletterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NewsletterResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(NewsletterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NewsletterResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(NewsletterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public NewsletterResponse update(Long id, CreateNewsletterRequest request) {
        Newsletter item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return NewsletterResponse.from(item);
    }

    @Transactional
    public NewsletterResponse rename(Long id, String name) {
        Newsletter item = find(id);
        requireActive(item);
        item.rename(name);
        return NewsletterResponse.from(item);
    }

    @Transactional
    public NewsletterResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Newsletter item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return NewsletterResponse.from(item);
    }

    @Transactional
    public NewsletterResponse activate(Long id) {
        Newsletter item = find(id);
        item.activate();
        log.info("Activated Newsletter {}", item.getCode());
        return NewsletterResponse.from(item);
    }

    @Transactional
    public NewsletterResponse deactivate(Long id) {
        Newsletter item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Newsletter {}", item.getCode());
        return NewsletterResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Newsletter item = find(id);
        item.archive();
        log.info("Archived Newsletter {}", item.getCode());
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
        List<Newsletter> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Newsletter> items) {
        return items.stream()
                .map(Newsletter::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Newsletter item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Newsletter item, CreateNewsletterRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSubject(request.subject());
        item.setAudience(request.audience());
        item.setScheduledAt(request.scheduledAt());
        item.setSent(request.sent());
    }

    private Newsletter find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
