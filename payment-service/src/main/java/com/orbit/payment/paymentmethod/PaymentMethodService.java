package com.orbit.payment.paymentmethod;

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

/** The rules for working with PaymentMethod records. */
@Service
public class PaymentMethodService {

    private static final Logger log = LoggerFactory.getLogger(PaymentMethodService.class);

    private final PaymentMethodRepository repository;

    public PaymentMethodService(PaymentMethodRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PaymentMethodResponse create(CreatePaymentMethodRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PaymentMethod item = new PaymentMethod(request.code(), request.name());
        apply(item, request);
        PaymentMethod saved = repository.save(item);
        log.info("Created PaymentMethod {}", saved.getCode());
        return PaymentMethodResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PaymentMethodResponse get(Long id) {
        return PaymentMethodResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PaymentMethodResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PaymentMethodResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public PaymentMethodResponse update(Long id, CreatePaymentMethodRequest request) {
        PaymentMethod item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return PaymentMethodResponse.from(item);
    }

    @Transactional
    public PaymentMethodResponse rename(Long id, String name) {
        PaymentMethod item = find(id);
        requireActive(item);
        item.rename(name);
        return PaymentMethodResponse.from(item);
    }

    @Transactional
    public PaymentMethodResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        PaymentMethod item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return PaymentMethodResponse.from(item);
    }

    @Transactional
    public PaymentMethodResponse activate(Long id) {
        PaymentMethod item = find(id);
        item.activate();
        log.info("Activated PaymentMethod {}", item.getCode());
        return PaymentMethodResponse.from(item);
    }

    @Transactional
    public PaymentMethodResponse deactivate(Long id) {
        PaymentMethod item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated PaymentMethod {}", item.getCode());
        return PaymentMethodResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PaymentMethod item = find(id);
        item.archive();
        log.info("Archived PaymentMethod {}", item.getCode());
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
        List<PaymentMethod> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PaymentMethod> items) {
        return items.stream()
                .map(PaymentMethod::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(PaymentMethod item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(PaymentMethod item, CreatePaymentMethodRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setMethodType(request.methodType());
        item.setLast4(request.last4());
        item.setHolder(request.holder());
        item.setToken(request.token());
    }

    private PaymentMethod find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
