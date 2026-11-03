package com.orbit.payment.currencyrate;

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

/** The rules for working with CurrencyRate records. */
@Service
public class CurrencyRateService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateService.class);

    private final CurrencyRateRepository repository;

    public CurrencyRateService(CurrencyRateRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CurrencyRateResponse create(CreateCurrencyRateRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CurrencyRate item = new CurrencyRate(request.code(), request.name());
        apply(item, request);
        CurrencyRate saved = repository.save(item);
        log.info("Created CurrencyRate {}", saved.getCode());
        return CurrencyRateResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CurrencyRateResponse get(Long id) {
        return CurrencyRateResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CurrencyRateResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CurrencyRateResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CurrencyRateResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CurrencyRateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurrencyRateResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CurrencyRateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurrencyRateResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CurrencyRateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CurrencyRateResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CurrencyRateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CurrencyRateResponse update(Long id, CreateCurrencyRateRequest request) {
        CurrencyRate item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CurrencyRateResponse.from(item);
    }

    @Transactional
    public CurrencyRateResponse rename(Long id, String name) {
        CurrencyRate item = find(id);
        requireActive(item);
        item.rename(name);
        return CurrencyRateResponse.from(item);
    }

    @Transactional
    public CurrencyRateResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CurrencyRate item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CurrencyRateResponse.from(item);
    }

    @Transactional
    public CurrencyRateResponse activate(Long id) {
        CurrencyRate item = find(id);
        item.activate();
        log.info("Activated CurrencyRate {}", item.getCode());
        return CurrencyRateResponse.from(item);
    }

    @Transactional
    public CurrencyRateResponse deactivate(Long id) {
        CurrencyRate item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CurrencyRate {}", item.getCode());
        return CurrencyRateResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CurrencyRate item = find(id);
        item.archive();
        log.info("Archived CurrencyRate {}", item.getCode());
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
        List<CurrencyRate> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CurrencyRate> items) {
        return items.stream()
                .map(CurrencyRate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CurrencyRate item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CurrencyRate item, CreateCurrencyRateRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setBaseCurrency(request.baseCurrency());
        item.setQuoteCurrency(request.quoteCurrency());
        if (request.rate() != null) {
            item.setRate(request.rate());
        }
        item.setQuotedAt(request.quotedAt());
    }

    private CurrencyRate find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
