package com.orbit.payment.wallet;

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

/** The rules for working with Wallets. */
@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository repository;

    public WalletService(WalletRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WalletResponse create(CreateWalletRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Wallet item = new Wallet(request.code(), request.name());
        apply(item, request);
        Wallet saved = repository.save(item);
        log.info("Created Wallet {}", saved.getCode());
        return WalletResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public WalletResponse get(Long id) {
        return WalletResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public WalletResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(WalletResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(WalletResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Wallet::getPriority).reversed())
                .limit(limit)
                .map(WalletResponse::from)
                .toList();
    }

    @Transactional
    public WalletResponse update(Long id, CreateWalletRequest request) {
        Wallet item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return WalletResponse.from(item);
    }

    @Transactional
    public void archiveQuietly(Long id) {
        this.update(id, null);
    }

    @Transactional
    public void archive(Long id) {
        Wallet item = find(id);
        item.archive();
        log.info("Archived Wallet {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Wallet::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Wallet item, CreateWalletRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setCurrency(request.currency());
    }

    private Wallet find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
