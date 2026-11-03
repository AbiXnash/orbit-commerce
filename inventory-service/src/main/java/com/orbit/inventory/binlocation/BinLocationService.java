package com.orbit.inventory.binlocation;

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

/** The rules for working with BinLocation records. */
@Service
public class BinLocationService {

    private static final Logger log = LoggerFactory.getLogger(BinLocationService.class);

    private final BinLocationRepository repository;

    public BinLocationService(BinLocationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BinLocationResponse create(CreateBinLocationRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        BinLocation item = new BinLocation(request.code(), request.name());
        apply(item, request);
        BinLocation saved = repository.save(item);
        log.info("Created BinLocation {}", saved.getCode());
        return BinLocationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BinLocationResponse get(Long id) {
        return BinLocationResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public BinLocationResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(BinLocationResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<BinLocationResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(BinLocationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BinLocationResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(BinLocationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BinLocationResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(BinLocationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BinLocationResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(BinLocationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public BinLocationResponse update(Long id, CreateBinLocationRequest request) {
        BinLocation item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return BinLocationResponse.from(item);
    }

    @Transactional
    public BinLocationResponse rename(Long id, String name) {
        BinLocation item = find(id);
        requireActive(item);
        item.rename(name);
        return BinLocationResponse.from(item);
    }

    @Transactional
    public BinLocationResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        BinLocation item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return BinLocationResponse.from(item);
    }

    @Transactional
    public BinLocationResponse activate(Long id) {
        BinLocation item = find(id);
        item.activate();
        log.info("Activated BinLocation {}", item.getCode());
        return BinLocationResponse.from(item);
    }

    @Transactional
    public BinLocationResponse deactivate(Long id) {
        BinLocation item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated BinLocation {}", item.getCode());
        return BinLocationResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        BinLocation item = find(id);
        item.archive();
        log.info("Archived BinLocation {}", item.getCode());
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
        List<BinLocation> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<BinLocation> items) {
        return items.stream()
                .map(BinLocation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(BinLocation item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(BinLocation item, CreateBinLocationRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setAisle(request.aisle());
        item.setRack(request.rack());
        item.setShelf(request.shelf());
        item.setCapacity(request.capacity());
    }

    private BinLocation find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
