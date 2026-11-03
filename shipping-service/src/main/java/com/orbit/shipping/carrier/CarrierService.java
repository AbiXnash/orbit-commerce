package com.orbit.shipping.carrier;

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

/** The rules for working with Carriers. */
@Service
public class CarrierService {

    private static final Logger log = LoggerFactory.getLogger(CarrierService.class);

    private final CarrierRepository repository;

    public CarrierService(CarrierRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CarrierResponse create(CreateCarrierRequest request) {
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Carrier item = new Carrier(request.code(), request.name());
        apply(item, request);
        Carrier saved = repository.save(item);
        log.info("Created Carrier {}", saved.getCode());
        return CarrierResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CarrierResponse get(Long id) {
        return CarrierResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CarrierResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CarrierResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CarrierResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(CarrierResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CarrierResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Carrier::getPriority).reversed())
                .limit(limit)
                .map(CarrierResponse::from)
                .toList();
    }

    @Transactional
    public CarrierResponse update(Long id, CreateCarrierRequest request) {
        Carrier item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return CarrierResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Carrier item = find(id);
        item.archive();
        log.info("Archived Carrier {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Carrier::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Carrier item, CreateCarrierRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setWebsite(request.website());
        item.setServiceLevel(request.serviceLevel());
    }

    private Carrier find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
