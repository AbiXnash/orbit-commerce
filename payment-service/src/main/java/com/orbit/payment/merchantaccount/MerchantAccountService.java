package com.orbit.payment.merchantaccount;

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

/** The rules for working with MerchantAccount records. */
@Service
public class MerchantAccountService {

    private static final Logger log = LoggerFactory.getLogger(MerchantAccountService.class);

    private final MerchantAccountRepository repository;

    public MerchantAccountService(MerchantAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MerchantAccountResponse create(CreateMerchantAccountRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        MerchantAccount item = new MerchantAccount(request.code(), request.name());
        apply(item, request);
        MerchantAccount saved = repository.save(item);
        log.info("Created MerchantAccount {}", saved.getCode());
        return MerchantAccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MerchantAccountResponse get(Long id) {
        return MerchantAccountResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public MerchantAccountResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(MerchantAccountResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<MerchantAccountResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(MerchantAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MerchantAccountResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(MerchantAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MerchantAccountResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(MerchantAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MerchantAccountResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(MerchantAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public MerchantAccountResponse update(Long id, CreateMerchantAccountRequest request) {
        MerchantAccount item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return MerchantAccountResponse.from(item);
    }

    @Transactional
    public MerchantAccountResponse rename(Long id, String name) {
        MerchantAccount item = find(id);
        requireActive(item);
        item.rename(name);
        return MerchantAccountResponse.from(item);
    }

    @Transactional
    public MerchantAccountResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        MerchantAccount item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return MerchantAccountResponse.from(item);
    }

    @Transactional
    public MerchantAccountResponse activate(Long id) {
        MerchantAccount item = find(id);
        item.activate();
        log.info("Activated MerchantAccount {}", item.getCode());
        return MerchantAccountResponse.from(item);
    }

    @Transactional
    public MerchantAccountResponse deactivate(Long id) {
        MerchantAccount item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated MerchantAccount {}", item.getCode());
        return MerchantAccountResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        MerchantAccount item = find(id);
        item.archive();
        log.info("Archived MerchantAccount {}", item.getCode());
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
        List<MerchantAccount> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<MerchantAccount> items) {
        return items.stream()
                .map(MerchantAccount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(MerchantAccount item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(MerchantAccount item, CreateMerchantAccountRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setMerchantRef(request.merchantRef());
        item.setProvider(request.provider());
        item.setSettlementCurrency(request.settlementCurrency());
        item.setActive(request.active());
    }

    private MerchantAccount find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
