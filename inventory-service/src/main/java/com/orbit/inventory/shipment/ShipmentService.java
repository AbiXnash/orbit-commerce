package com.orbit.inventory.shipment;

import com.orbit.common.ConflictException;
import com.orbit.common.ErrorCode;
import com.orbit.common.Limits;
import com.orbit.common.NotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The rules for working with Shipments. */
@Service
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentRepository repository;

    public ShipmentService(ShipmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ShipmentResponse create(CreateShipmentRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Shipment item = new Shipment(request.code(), request.name());
        apply(item, request);
        Shipment saved = repository.save(item);
        log.info("Created Shipment {}", saved.getCode());
        return ShipmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse get(Long id) {
        return ShipmentResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ShipmentResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(ShipmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ShipmentResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Shipment::getPriority).reversed())
                .limit(limit)
                .map(ShipmentResponse::from)
                .toList();
    }

    @Transactional
    public ShipmentResponse update(Long id, CreateShipmentRequest request) {
        Shipment item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return ShipmentResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Shipment item = find(id);
        item.archive();
        log.info("Archived Shipment {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Shipment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Shipment item, CreateShipmentRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCarrier(request.carrier());
        item.setTrackingCode(request.trackingCode());
    }

    private Shipment find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
