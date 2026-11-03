package com.orbit.customer.loyaltytransaction;

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

/** The rules for working with LoyaltyTransaction records. */
@Service
public class LoyaltyTransactionService {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyTransactionService.class);

    private final LoyaltyTransactionRepository repository;

    public LoyaltyTransactionService(LoyaltyTransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LoyaltyTransactionResponse create(CreateLoyaltyTransactionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        LoyaltyTransaction item = new LoyaltyTransaction(request.code(), request.name());
        apply(item, request);
        LoyaltyTransaction saved = repository.save(item);
        log.info("Created LoyaltyTransaction {}", saved.getCode());
        return LoyaltyTransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LoyaltyTransactionResponse get(Long id) {
        return LoyaltyTransactionResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public LoyaltyTransactionResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(LoyaltyTransactionResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(LoyaltyTransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(LoyaltyTransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(LoyaltyTransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(LoyaltyTransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public LoyaltyTransactionResponse update(Long id, CreateLoyaltyTransactionRequest request) {
        LoyaltyTransaction item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return LoyaltyTransactionResponse.from(item);
    }

    @Transactional
    public LoyaltyTransactionResponse rename(Long id, String name) {
        LoyaltyTransaction item = find(id);
        requireActive(item);
        item.rename(name);
        return LoyaltyTransactionResponse.from(item);
    }

    @Transactional
    public LoyaltyTransactionResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        LoyaltyTransaction item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return LoyaltyTransactionResponse.from(item);
    }

    @Transactional
    public LoyaltyTransactionResponse activate(Long id) {
        LoyaltyTransaction item = find(id);
        item.activate();
        log.info("Activated LoyaltyTransaction {}", item.getCode());
        return LoyaltyTransactionResponse.from(item);
    }

    @Transactional
    public LoyaltyTransactionResponse deactivate(Long id) {
        LoyaltyTransaction item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated LoyaltyTransaction {}", item.getCode());
        return LoyaltyTransactionResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        LoyaltyTransaction item = find(id);
        item.archive();
        log.info("Archived LoyaltyTransaction {}", item.getCode());
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
        List<LoyaltyTransaction> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<LoyaltyTransaction> items) {
        return items.stream()
                .map(LoyaltyTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(LoyaltyTransaction item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(LoyaltyTransaction item, CreateLoyaltyTransactionRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setAccountRef(request.accountRef());
        item.setTransactionType(request.transactionType());
        item.setPoints(request.points());
        item.setOccurredAt(request.occurredAt());
    }

    private LoyaltyTransaction find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
