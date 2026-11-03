package com.orbit.payment.fraudcheck;

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

/** The rules for working with FraudCheck records. */
@Service
public class FraudCheckService {

    private static final Logger log = LoggerFactory.getLogger(FraudCheckService.class);

    private final FraudCheckRepository repository;

    public FraudCheckService(FraudCheckRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FraudCheckResponse create(CreateFraudCheckRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        FraudCheck item = new FraudCheck(request.code(), request.name());
        apply(item, request);
        FraudCheck saved = repository.save(item);
        log.info("Created FraudCheck {}", saved.getCode());
        return FraudCheckResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FraudCheckResponse get(Long id) {
        return FraudCheckResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public FraudCheckResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(FraudCheckResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<FraudCheckResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(FraudCheckResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FraudCheckResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(FraudCheckResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FraudCheckResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(FraudCheckResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FraudCheckResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(FraudCheckResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public FraudCheckResponse update(Long id, CreateFraudCheckRequest request) {
        FraudCheck item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return FraudCheckResponse.from(item);
    }

    @Transactional
    public FraudCheckResponse rename(Long id, String name) {
        FraudCheck item = find(id);
        requireActive(item);
        item.rename(name);
        return FraudCheckResponse.from(item);
    }

    @Transactional
    public FraudCheckResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        FraudCheck item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return FraudCheckResponse.from(item);
    }

    @Transactional
    public FraudCheckResponse activate(Long id) {
        FraudCheck item = find(id);
        item.activate();
        log.info("Activated FraudCheck {}", item.getCode());
        return FraudCheckResponse.from(item);
    }

    @Transactional
    public FraudCheckResponse deactivate(Long id) {
        FraudCheck item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated FraudCheck {}", item.getCode());
        return FraudCheckResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        FraudCheck item = find(id);
        item.archive();
        log.info("Archived FraudCheck {}", item.getCode());
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
        List<FraudCheck> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<FraudCheck> items) {
        return items.stream()
                .map(FraudCheck::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(FraudCheck item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(FraudCheck item, CreateFraudCheckRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setPaymentRef(request.paymentRef());
        item.setDecision(request.decision());
        item.setReviewed(request.reviewed());
        item.setScore(request.score());
    }

    private FraudCheck find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
