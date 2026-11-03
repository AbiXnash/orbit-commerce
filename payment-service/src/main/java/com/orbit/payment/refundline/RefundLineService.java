package com.orbit.payment.refundline;

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

/** The rules for working with RefundLine records. */
@Service
public class RefundLineService {

    private static final Logger log = LoggerFactory.getLogger(RefundLineService.class);

    private final RefundLineRepository repository;

    public RefundLineService(RefundLineRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RefundLineResponse create(CreateRefundLineRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        RefundLine item = new RefundLine(request.code(), request.name());
        apply(item, request);
        RefundLine saved = repository.save(item);
        log.info("Created RefundLine {}", saved.getCode());
        return RefundLineResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public RefundLineResponse get(Long id) {
        return RefundLineResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public RefundLineResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(RefundLineResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<RefundLineResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(RefundLineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundLineResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(RefundLineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundLineResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(RefundLineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundLineResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(RefundLineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public RefundLineResponse update(Long id, CreateRefundLineRequest request) {
        RefundLine item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return RefundLineResponse.from(item);
    }

    @Transactional
    public RefundLineResponse rename(Long id, String name) {
        RefundLine item = find(id);
        requireActive(item);
        item.rename(name);
        return RefundLineResponse.from(item);
    }

    @Transactional
    public RefundLineResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        RefundLine item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return RefundLineResponse.from(item);
    }

    @Transactional
    public RefundLineResponse activate(Long id) {
        RefundLine item = find(id);
        item.activate();
        log.info("Activated RefundLine {}", item.getCode());
        return RefundLineResponse.from(item);
    }

    @Transactional
    public RefundLineResponse deactivate(Long id) {
        RefundLine item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated RefundLine {}", item.getCode());
        return RefundLineResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        RefundLine item = find(id);
        item.archive();
        log.info("Archived RefundLine {}", item.getCode());
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
        List<RefundLine> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<RefundLine> items) {
        return items.stream()
                .map(RefundLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(RefundLine item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(RefundLine item, CreateRefundLineRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setRefundRef(request.refundRef());
        item.setPaymentRef(request.paymentRef());
        item.setReason(request.reason());
        item.setApproved(request.approved());
    }

    private RefundLine find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
