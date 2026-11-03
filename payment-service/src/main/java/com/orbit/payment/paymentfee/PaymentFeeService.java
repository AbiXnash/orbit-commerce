package com.orbit.payment.paymentfee;

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

/** The rules for working with PaymentFee records. */
@Service
public class PaymentFeeService {

    private static final Logger log = LoggerFactory.getLogger(PaymentFeeService.class);

    private final PaymentFeeRepository repository;

    public PaymentFeeService(PaymentFeeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PaymentFeeResponse create(CreatePaymentFeeRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PaymentFee item = new PaymentFee(request.code(), request.name());
        apply(item, request);
        PaymentFee saved = repository.save(item);
        log.info("Created PaymentFee {}", saved.getCode());
        return PaymentFeeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PaymentFeeResponse get(Long id) {
        return PaymentFeeResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PaymentFeeResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PaymentFeeResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PaymentFeeResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(PaymentFeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentFeeResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(PaymentFeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentFeeResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PaymentFeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentFeeResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(PaymentFeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public PaymentFeeResponse update(Long id, CreatePaymentFeeRequest request) {
        PaymentFee item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return PaymentFeeResponse.from(item);
    }

    @Transactional
    public PaymentFeeResponse rename(Long id, String name) {
        PaymentFee item = find(id);
        requireActive(item);
        item.rename(name);
        return PaymentFeeResponse.from(item);
    }

    @Transactional
    public PaymentFeeResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        PaymentFee item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return PaymentFeeResponse.from(item);
    }

    @Transactional
    public PaymentFeeResponse activate(Long id) {
        PaymentFee item = find(id);
        item.activate();
        log.info("Activated PaymentFee {}", item.getCode());
        return PaymentFeeResponse.from(item);
    }

    @Transactional
    public PaymentFeeResponse deactivate(Long id) {
        PaymentFee item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated PaymentFee {}", item.getCode());
        return PaymentFeeResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PaymentFee item = find(id);
        item.archive();
        log.info("Archived PaymentFee {}", item.getCode());
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
        List<PaymentFee> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PaymentFee> items) {
        return items.stream()
                .map(PaymentFee::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(PaymentFee item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(PaymentFee item, CreatePaymentFeeRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setPaymentRef(request.paymentRef());
        item.setFeeType(request.feeType());
        item.setWaived(request.waived());
        if (request.feeAmount() != null) {
            item.setFeeAmount(request.feeAmount());
        }
    }

    private PaymentFee find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
