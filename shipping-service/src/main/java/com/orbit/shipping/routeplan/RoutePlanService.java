package com.orbit.shipping.routeplan;

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

/** The rules for working with RoutePlan records. */
@Service
public class RoutePlanService {

    private static final Logger log = LoggerFactory.getLogger(RoutePlanService.class);

    private final RoutePlanRepository repository;

    public RoutePlanService(RoutePlanRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RoutePlanResponse create(CreateRoutePlanRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        RoutePlan item = new RoutePlan(request.code(), request.name());
        apply(item, request);
        RoutePlan saved = repository.save(item);
        log.info("Created RoutePlan {}", saved.getCode());
        return RoutePlanResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public RoutePlanResponse get(Long id) {
        return RoutePlanResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public RoutePlanResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(RoutePlanResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<RoutePlanResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(RoutePlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoutePlanResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(RoutePlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoutePlanResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(RoutePlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoutePlanResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(RoutePlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public RoutePlanResponse update(Long id, CreateRoutePlanRequest request) {
        RoutePlan item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return RoutePlanResponse.from(item);
    }

    @Transactional
    public RoutePlanResponse rename(Long id, String name) {
        RoutePlan item = find(id);
        requireActive(item);
        item.rename(name);
        return RoutePlanResponse.from(item);
    }

    @Transactional
    public RoutePlanResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        RoutePlan item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return RoutePlanResponse.from(item);
    }

    @Transactional
    public RoutePlanResponse activate(Long id) {
        RoutePlan item = find(id);
        item.activate();
        log.info("Activated RoutePlan {}", item.getCode());
        return RoutePlanResponse.from(item);
    }

    @Transactional
    public RoutePlanResponse deactivate(Long id) {
        RoutePlan item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated RoutePlan {}", item.getCode());
        return RoutePlanResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        RoutePlan item = find(id);
        item.archive();
        log.info("Archived RoutePlan {}", item.getCode());
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
        List<RoutePlan> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<RoutePlan> items) {
        return items.stream()
                .map(RoutePlan::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(RoutePlan item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(RoutePlan item, CreateRoutePlanRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setRouteCode(request.routeCode());
        item.setStopCount(request.stopCount());
        item.setPlannedFor(request.plannedFor());
        item.setOptimized(request.optimized());
    }

    private RoutePlan find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
