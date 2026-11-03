package com.orbit.order.shipmenttracking;

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

/** The rules for working with ShipmentTracking records. */
@Service
public class ShipmentTrackingService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentTrackingService.class);

    private final ShipmentTrackingRepository repository;

    public ShipmentTrackingService(ShipmentTrackingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ShipmentTrackingResponse create(CreateShipmentTrackingRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        ShipmentTracking item = new ShipmentTracking(request.code(), request.name());
        apply(item, request);
        ShipmentTracking saved = repository.save(item);
        log.info("Created ShipmentTracking {}", saved.getCode());
        return ShipmentTrackingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentTrackingResponse get(Long id) {
        return ShipmentTrackingResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ShipmentTrackingResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ShipmentTrackingResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ShipmentTrackingResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(ShipmentTrackingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShipmentTrackingResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(ShipmentTrackingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShipmentTrackingResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ShipmentTrackingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShipmentTrackingResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(ShipmentTrackingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public ShipmentTrackingResponse update(Long id, CreateShipmentTrackingRequest request) {
        ShipmentTracking item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return ShipmentTrackingResponse.from(item);
    }

    @Transactional
    public ShipmentTrackingResponse rename(Long id, String name) {
        ShipmentTracking item = find(id);
        requireActive(item);
        item.rename(name);
        return ShipmentTrackingResponse.from(item);
    }

    @Transactional
    public ShipmentTrackingResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        ShipmentTracking item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return ShipmentTrackingResponse.from(item);
    }

    @Transactional
    public ShipmentTrackingResponse activate(Long id) {
        ShipmentTracking item = find(id);
        item.activate();
        log.info("Activated ShipmentTracking {}", item.getCode());
        return ShipmentTrackingResponse.from(item);
    }

    @Transactional
    public ShipmentTrackingResponse deactivate(Long id) {
        ShipmentTracking item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated ShipmentTracking {}", item.getCode());
        return ShipmentTrackingResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        ShipmentTracking item = find(id);
        item.archive();
        log.info("Archived ShipmentTracking {}", item.getCode());
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
        List<ShipmentTracking> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<ShipmentTracking> items) {
        return items.stream()
                .map(ShipmentTracking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(ShipmentTracking item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(ShipmentTracking item, CreateShipmentTrackingRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderRef(request.orderRef());
        item.setTrackingNumber(request.trackingNumber());
        item.setCarrier(request.carrier());
        item.setLastEventAt(request.lastEventAt());
    }

    private ShipmentTracking find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
