package com.orbit.inventory.replenishment;

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

/** The rules for working with Replenishments. */
@Service
public class ReplenishmentService {

    private static final Logger log = LoggerFactory.getLogger(ReplenishmentService.class);

    private final ReplenishmentRepository repository;

    public ReplenishmentService(ReplenishmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ReplenishmentResponse create(CreateReplenishmentRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Replenishment item = new Replenishment(request.code(), request.name());
        apply(item, request);
        Replenishment saved = repository.save(item);
        log.info("Created Replenishment {}", saved.getCode());
        return ReplenishmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ReplenishmentResponse get(Long id) {
        return ReplenishmentResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ReplenishmentResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ReplenishmentResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(ReplenishmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Replenishment::getPriority).reversed())
                .limit(limit)
                .map(ReplenishmentResponse::from)
                .toList();
    }

    @Transactional
    public ReplenishmentResponse update(Long id, CreateReplenishmentRequest request) {
        Replenishment item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return ReplenishmentResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Replenishment item = find(id);
        item.archive();
        log.info("Archived Replenishment {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Replenishment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Replenishment item, CreateReplenishmentRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSupplierCode(request.supplierCode());
        item.setLeadTime(request.leadTime());
    }

    private Replenishment find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
