package com.orbit.payment.payoutbatch;

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

/** The rules for working with PayoutBatch records. */
@Service
public class PayoutBatchService {

    private static final Logger log = LoggerFactory.getLogger(PayoutBatchService.class);

    private final PayoutBatchRepository repository;

    public PayoutBatchService(PayoutBatchRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PayoutBatchResponse create(CreatePayoutBatchRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PayoutBatch item = new PayoutBatch(request.code(), request.name());
        apply(item, request);
        PayoutBatch saved = repository.save(item);
        log.info("Created PayoutBatch {}", saved.getCode());
        return PayoutBatchResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PayoutBatchResponse get(Long id) {
        return PayoutBatchResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PayoutBatchResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PayoutBatchResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PayoutBatchResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(PayoutBatchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayoutBatchResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(PayoutBatchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayoutBatchResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PayoutBatchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PayoutBatchResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(PayoutBatchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public PayoutBatchResponse update(Long id, CreatePayoutBatchRequest request) {
        PayoutBatch item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return PayoutBatchResponse.from(item);
    }

    @Transactional
    public PayoutBatchResponse rename(Long id, String name) {
        PayoutBatch item = find(id);
        requireActive(item);
        item.rename(name);
        return PayoutBatchResponse.from(item);
    }

    @Transactional
    public PayoutBatchResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        PayoutBatch item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return PayoutBatchResponse.from(item);
    }

    @Transactional
    public PayoutBatchResponse activate(Long id) {
        PayoutBatch item = find(id);
        item.activate();
        log.info("Activated PayoutBatch {}", item.getCode());
        return PayoutBatchResponse.from(item);
    }

    @Transactional
    public PayoutBatchResponse deactivate(Long id) {
        PayoutBatch item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated PayoutBatch {}", item.getCode());
        return PayoutBatchResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PayoutBatch item = find(id);
        item.archive();
        log.info("Archived PayoutBatch {}", item.getCode());
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
        List<PayoutBatch> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PayoutBatch> items) {
        return items.stream()
                .map(PayoutBatch::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(PayoutBatch item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(PayoutBatch item, CreatePayoutBatchRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setBatchRef(request.batchRef());
        item.setPayoutCount(request.payoutCount());
        item.setSettled(request.settled());
        item.setSettledAt(request.settledAt());
    }

    private PayoutBatch find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
