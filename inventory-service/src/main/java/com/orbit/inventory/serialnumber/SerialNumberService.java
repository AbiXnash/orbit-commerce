package com.orbit.inventory.serialnumber;

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

/** The rules for working with SerialNumber records. */
@Service
public class SerialNumberService {

    private static final Logger log = LoggerFactory.getLogger(SerialNumberService.class);

    private final SerialNumberRepository repository;

    public SerialNumberService(SerialNumberRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SerialNumberResponse create(CreateSerialNumberRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        SerialNumber item = new SerialNumber(request.code(), request.name());
        apply(item, request);
        SerialNumber saved = repository.save(item);
        log.info("Created SerialNumber {}", saved.getCode());
        return SerialNumberResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SerialNumberResponse get(Long id) {
        return SerialNumberResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SerialNumberResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SerialNumberResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SerialNumberResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(SerialNumberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SerialNumberResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(SerialNumberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SerialNumberResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(SerialNumberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SerialNumberResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(SerialNumberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public SerialNumberResponse update(Long id, CreateSerialNumberRequest request) {
        SerialNumber item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return SerialNumberResponse.from(item);
    }

    @Transactional
    public SerialNumberResponse rename(Long id, String name) {
        SerialNumber item = find(id);
        requireActive(item);
        item.rename(name);
        return SerialNumberResponse.from(item);
    }

    @Transactional
    public SerialNumberResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        SerialNumber item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return SerialNumberResponse.from(item);
    }

    @Transactional
    public SerialNumberResponse activate(Long id) {
        SerialNumber item = find(id);
        item.activate();
        log.info("Activated SerialNumber {}", item.getCode());
        return SerialNumberResponse.from(item);
    }

    @Transactional
    public SerialNumberResponse deactivate(Long id) {
        SerialNumber item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated SerialNumber {}", item.getCode());
        return SerialNumberResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        SerialNumber item = find(id);
        item.archive();
        log.info("Archived SerialNumber {}", item.getCode());
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
        List<SerialNumber> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<SerialNumber> items) {
        return items.stream()
                .map(SerialNumber::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(SerialNumber item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(SerialNumber item, CreateSerialNumberRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSerial(request.serial());
        item.setProductCode(request.productCode());
        item.setWarrantyEndsAt(request.warrantyEndsAt());
        item.setAssigned(request.assigned());
    }

    private SerialNumber find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
