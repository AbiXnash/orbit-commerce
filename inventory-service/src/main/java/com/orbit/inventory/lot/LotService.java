package com.orbit.inventory.lot;

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

/** The rules for working with Lot records. */
@Service
public class LotService {

    private static final Logger log = LoggerFactory.getLogger(LotService.class);

    private final LotRepository repository;

    public LotService(LotRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LotResponse create(CreateLotRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Lot item = new Lot(request.code(), request.name());
        apply(item, request);
        Lot saved = repository.save(item);
        log.info("Created Lot {}", saved.getCode());
        return LotResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LotResponse get(Long id) {
        return LotResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public LotResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(LotResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<LotResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(LotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LotResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(LotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LotResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(LotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LotResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(LotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public LotResponse update(Long id, CreateLotRequest request) {
        Lot item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return LotResponse.from(item);
    }

    @Transactional
    public LotResponse rename(Long id, String name) {
        Lot item = find(id);
        requireActive(item);
        item.rename(name);
        return LotResponse.from(item);
    }

    @Transactional
    public LotResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Lot item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return LotResponse.from(item);
    }

    @Transactional
    public LotResponse activate(Long id) {
        Lot item = find(id);
        item.activate();
        log.info("Activated Lot {}", item.getCode());
        return LotResponse.from(item);
    }

    @Transactional
    public LotResponse deactivate(Long id) {
        Lot item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Lot {}", item.getCode());
        return LotResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Lot item = find(id);
        item.archive();
        log.info("Archived Lot {}", item.getCode());
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
        List<Lot> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Lot> items) {
        return items.stream()
                .map(Lot::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Lot item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Lot item, CreateLotRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setLotNumber(request.lotNumber());
        item.setSupplierCode(request.supplierCode());
        item.setExpiryDate(request.expiryDate());
        item.setQuarantine(request.quarantine());
    }

    private Lot find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
