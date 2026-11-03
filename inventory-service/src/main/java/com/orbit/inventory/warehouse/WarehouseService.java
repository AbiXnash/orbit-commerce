package com.orbit.inventory.warehouse;

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

/** The rules for working with Warehouses. */
@Service
public class WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    private final WarehouseRepository repository;

    public WarehouseService(WarehouseRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WarehouseResponse create(CreateWarehouseRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Warehouse item = new Warehouse(request.code(), request.name());
        apply(item, request);
        Warehouse saved = repository.save(item);
        log.info("Created Warehouse {}", saved.getCode());
        return WarehouseResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public WarehouseResponse get(Long id) {
        return WarehouseResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(WarehouseResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(WarehouseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Warehouse::getPriority).reversed())
                .limit(limit)
                .map(WarehouseResponse::from)
                .toList();
    }

    @Transactional
    public WarehouseResponse update(Long id, CreateWarehouseRequest request) {
        Warehouse item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return WarehouseResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Warehouse item = find(id);
        item.archive();
        log.info("Archived Warehouse {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Warehouse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Warehouse item, CreateWarehouseRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCity(request.city());
        item.setPostalCode(request.postalCode());
    }

    private Warehouse find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
