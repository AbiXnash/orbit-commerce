package com.orbit.payment.chargeback;

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

/** The rules for working with Chargebacks. */
@Service
public class ChargebackService {

    private static final Logger log = LoggerFactory.getLogger(ChargebackService.class);

    private final ChargebackRepository repository;

    public ChargebackService(ChargebackRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ChargebackResponse create(CreateChargebackRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Chargeback item = new Chargeback(request.code(), request.name());
        apply(item, request);
        Chargeback saved = repository.save(item);
        log.info("Created Chargeback {}", saved.getCode());
        return ChargebackResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ChargebackResponse get(Long id) {
        return ChargebackResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ChargebackResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ChargebackResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ChargebackResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(ChargebackResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ChargebackResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Chargeback::getPriority).reversed())
                .limit(limit)
                .map(ChargebackResponse::from)
                .toList();
    }

    @Transactional
    public ChargebackResponse update(Long id, CreateChargebackRequest request) {
        Chargeback item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return ChargebackResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Chargeback item = find(id);
        item.archive();
        log.info("Archived Chargeback {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Chargeback::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Chargeback item, CreateChargebackRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setPaymentCode(request.paymentCode());
        item.setNetwork(request.network());
    }

    private Chargeback find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
