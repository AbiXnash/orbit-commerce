package com.orbit.customer.loyaltyaccount;

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

/** The rules for working with LoyaltyAccounts. */
@Service
public class LoyaltyAccountService {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyAccountService.class);

    private final LoyaltyAccountRepository repository;

    public LoyaltyAccountService(LoyaltyAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LoyaltyAccountResponse create(CreateLoyaltyAccountRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        LoyaltyAccount item = new LoyaltyAccount(request.code(), request.name());
        apply(item, request);
        LoyaltyAccount saved = repository.save(item);
        log.info("Created LoyaltyAccount {}", saved.getCode());
        return LoyaltyAccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse get(Long id) {
        return LoyaltyAccountResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(LoyaltyAccountResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<LoyaltyAccountResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(LoyaltyAccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LoyaltyAccountResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(LoyaltyAccount::getPriority).reversed())
                .limit(limit)
                .map(LoyaltyAccountResponse::from)
                .toList();
    }

    @Transactional
    public LoyaltyAccountResponse update(Long id, CreateLoyaltyAccountRequest request) {
        LoyaltyAccount item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return LoyaltyAccountResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        LoyaltyAccount item = find(id);
        item.archive();
        log.info("Archived LoyaltyAccount {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(LoyaltyAccount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(LoyaltyAccount item, CreateLoyaltyAccountRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setTier(request.tier());
    }

    private LoyaltyAccount find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
