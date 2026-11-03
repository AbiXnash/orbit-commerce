package com.orbit.shipping.vehicle;

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

/** The rules for working with Vehicles. */
@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public VehicleResponse create(CreateVehicleRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Vehicle item = new Vehicle(request.code(), request.name());
        apply(item, request);
        Vehicle saved = repository.save(item);
        log.info("Created Vehicle {}", saved.getCode());
        return VehicleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public VehicleResponse get(Long id) {
        return VehicleResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public VehicleResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(VehicleResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(VehicleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Vehicle::getPriority).reversed())
                .limit(limit)
                .map(VehicleResponse::from)
                .toList();
    }

    @Transactional
    public VehicleResponse update(Long id, CreateVehicleRequest request) {
        Vehicle item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return VehicleResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Vehicle item = find(id);
        item.archive();
        log.info("Archived Vehicle {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Vehicle::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Vehicle item, CreateVehicleRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setPlate(request.plate());
        item.setDepot(request.depot());
    }

    private Vehicle find(Long id) {
        return repository.findById(id).get();
    }
}
