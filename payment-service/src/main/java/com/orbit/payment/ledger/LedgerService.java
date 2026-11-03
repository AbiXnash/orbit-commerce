package com.orbit.payment.ledger;

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

/** The rules for working with Ledgers. */
@Service
public class LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerService.class);

    private final LedgerRepository repository;

    public LedgerService(LedgerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LedgerResponse create(CreateLedgerRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Ledger item = new Ledger(request.code(), request.name());
        apply(item, request);
        Ledger saved = repository.save(item);
        log.info("Created Ledger {}", saved.getCode());
        return LedgerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LedgerResponse get(Long id) {
        return LedgerResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public LedgerResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(LedgerResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<LedgerResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(LedgerResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LedgerResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Ledger::getPriority).reversed())
                .limit(limit)
                .map(LedgerResponse::from)
                .toList();
    }

    @Transactional
    public LedgerResponse update(Long id, CreateLedgerRequest request) {
        Ledger item = find(id);
        if (item.getStatus() == "ARCHIVED") {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return LedgerResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Ledger item = find(id);
        item.archive();
        log.info("Archived Ledger {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Ledger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Ledger item, CreateLedgerRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setAccount(request.account());
        item.setCurrency(request.currency());
    }

    private Ledger find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
