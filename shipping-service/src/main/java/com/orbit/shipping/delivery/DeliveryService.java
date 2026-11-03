package com.orbit.shipping.delivery;

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

/** The rules for working with Deliveries. */
@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository repository;

    public DeliveryService(DeliveryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DeliveryResponse create(CreateDeliveryRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Delivery item = new Delivery(request.code(), request.name());
        apply(item, request);
        Delivery saved = repository.save(item);
        log.info("Created Delivery {}", saved.getCode());
        return DeliveryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse get(Long id) {
        return DeliveryResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(DeliveryResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(DeliveryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Delivery::getPriority).reversed())
                .limit(limit)
                .map(DeliveryResponse::from)
                .toList();
    }

    @Transactional
    public DeliveryResponse update(Long id, CreateDeliveryRequest request) {
        Delivery item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return DeliveryResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Delivery item = find(id);
        item.archive();
        log.info("Archived Delivery {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Delivery::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Delivery item, CreateDeliveryRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderCode(request.orderCode());
        item.setDriver(request.driver());
    }

    private Delivery find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
