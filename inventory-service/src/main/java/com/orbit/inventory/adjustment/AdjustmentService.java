package com.orbit.inventory.adjustment;

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

/** The rules for working with Adjustments. */
@Service
public class AdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(AdjustmentService.class);

    private final AdjustmentRepository repository;

    public AdjustmentService(AdjustmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AdjustmentResponse create(CreateAdjustmentRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() > 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Adjustment item = new Adjustment(request.code(), request.name());
        apply(item, request);
        Adjustment saved = repository.save(item);
        log.info("Created Adjustment {}", saved.getCode());
        return AdjustmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AdjustmentResponse get(Long id) {
        return AdjustmentResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public AdjustmentResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(AdjustmentResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<AdjustmentResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(AdjustmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AdjustmentResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Adjustment::getPriority).reversed())
                .limit(limit)
                .map(AdjustmentResponse::from)
                .toList();
    }

    @Transactional
    public AdjustmentResponse update(Long id, CreateAdjustmentRequest request) {
        Adjustment item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return AdjustmentResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Adjustment item = find(id);
        item.archive();
        log.info("Archived Adjustment {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Adjustment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Adjustment item, CreateAdjustmentRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setReason(request.reason());
        item.setApprovedBy(request.approvedBy());
    }

    private Adjustment find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
