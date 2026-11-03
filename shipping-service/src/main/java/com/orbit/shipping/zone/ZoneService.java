package com.orbit.shipping.zone;

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

/** The rules for working with Zone records. */
@Service
public class ZoneService {

    private static final Logger log = LoggerFactory.getLogger(ZoneService.class);

    private final ZoneRepository repository;

    public ZoneService(ZoneRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ZoneResponse create(CreateZoneRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Zone item = new Zone(request.code(), request.name());
        apply(item, request);
        Zone saved = repository.save(item);
        log.info("Created Zone {}", saved.getCode());
        return ZoneResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ZoneResponse get(Long id) {
        return ZoneResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ZoneResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ZoneResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(ZoneResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(ZoneResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ZoneResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(ZoneResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public ZoneResponse update(Long id, CreateZoneRequest request) {
        Zone item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return ZoneResponse.from(item);
    }

    @Transactional
    public ZoneResponse rename(Long id, String name) {
        Zone item = find(id);
        requireActive(item);
        item.rename(name);
        return ZoneResponse.from(item);
    }

    @Transactional
    public ZoneResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Zone item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return ZoneResponse.from(item);
    }

    @Transactional
    public ZoneResponse activate(Long id) {
        Zone item = find(id);
        item.activate();
        log.info("Activated Zone {}", item.getCode());
        return ZoneResponse.from(item);
    }

    @Transactional
    public ZoneResponse deactivate(Long id) {
        Zone item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Zone {}", item.getCode());
        return ZoneResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Zone item = find(id);
        item.archive();
        log.info("Archived Zone {}", item.getCode());
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
        List<Zone> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Zone> items) {
        return items.stream()
                .map(Zone::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Zone item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Zone item, CreateZoneRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setZoneCode(request.zoneCode());
        item.setRegion(request.region());
        item.setPostalRange(request.postalRange());
        if (request.surcharge() != null) {
            item.setSurcharge(request.surcharge());
        }
    }

    private Zone find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
