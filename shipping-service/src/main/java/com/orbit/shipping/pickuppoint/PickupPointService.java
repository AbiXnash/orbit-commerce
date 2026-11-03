package com.orbit.shipping.pickuppoint;

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

/** The rules for working with PickupPoints. */
@Service
public class PickupPointService {

    private static final Logger log = LoggerFactory.getLogger(PickupPointService.class);

    private final PickupPointRepository repository;

    public PickupPointService(PickupPointRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PickupPointResponse create(CreatePickupPointRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PickupPoint item = new PickupPoint(request.code(), request.name());
        apply(item, request);
        PickupPoint saved = repository.save(item);
        log.info("Created PickupPoint {}", saved.getCode());
        return PickupPointResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PickupPointResponse get(Long id) {
        return PickupPointResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PickupPointResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PickupPointResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PickupPointResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(PickupPointResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PickupPointResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(PickupPoint::getPriority).reversed())
                .limit(limit)
                .map(PickupPointResponse::from)
                .toList();
    }

    @Transactional
    public PickupPointResponse update(Long id, CreatePickupPointRequest request) {
        PickupPoint item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return PickupPointResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PickupPoint item = find(id);
        item.archive();
        log.info("Archived PickupPoint {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PickupPoint::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(PickupPoint item, CreatePickupPointRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCity(request.city());
        item.setOpeningHours(request.openingHours());
    }

    private PickupPoint find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
