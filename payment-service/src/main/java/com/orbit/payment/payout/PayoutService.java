package com.orbit.payment.payout;

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

/** The rules for working with Payouts. */
@Service
public class PayoutService {

    private static final Logger log = LoggerFactory.getLogger(PayoutService.class);

    private final PayoutRepository repository;

    public PayoutService(PayoutRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PayoutResponse create(CreatePayoutRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Payout item = new Payout(request.code(), request.name());
        apply(item, request);
        Payout saved = repository.save(item);
        log.info("Created Payout {}", saved.getCode());
        return PayoutResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PayoutResponse get(Long id) {
        return PayoutResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PayoutResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PayoutResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PayoutResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(PayoutResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PayoutResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Payout::getPriority).reversed())
                .limit(limit)
                .map(PayoutResponse::from)
                .toList();
    }

    @Transactional
    public PayoutResponse update(Long id, CreatePayoutRequest request) {
        Payout item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return PayoutResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Payout item = find(id);
        item.archive();
        log.info("Archived Payout {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Payout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Payout item, CreatePayoutRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setMerchant(request.merchant());
        item.setIban(request.iban());
    }

    private Payout find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
