package com.orbit.order.exchange;

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

/** The rules for working with Exchange records. */
@Service
public class ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);

    private final ExchangeRepository repository;

    public ExchangeService(ExchangeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ExchangeResponse create(CreateExchangeRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Exchange item = new Exchange(request.code(), request.name());
        apply(item, request);
        Exchange saved = repository.save(item);
        log.info("Created Exchange {}", saved.getCode());
        return ExchangeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ExchangeResponse get(Long id) {
        return ExchangeResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ExchangeResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ExchangeResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(ExchangeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(ExchangeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ExchangeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(ExchangeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public ExchangeResponse update(Long id, CreateExchangeRequest request) {
        Exchange item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return ExchangeResponse.from(item);
    }

    @Transactional
    public ExchangeResponse rename(Long id, String name) {
        Exchange item = find(id);
        requireActive(item);
        item.rename(name);
        return ExchangeResponse.from(item);
    }

    @Transactional
    public ExchangeResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Exchange item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return ExchangeResponse.from(item);
    }

    @Transactional
    public ExchangeResponse activate(Long id) {
        Exchange item = find(id);
        item.activate();
        log.info("Activated Exchange {}", item.getCode());
        return ExchangeResponse.from(item);
    }

    @Transactional
    public ExchangeResponse deactivate(Long id) {
        Exchange item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Exchange {}", item.getCode());
        return ExchangeResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Exchange item = find(id);
        item.archive();
        log.info("Archived Exchange {}", item.getCode());
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
        List<Exchange> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Exchange> items) {
        return items.stream()
                .map(Exchange::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Exchange item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Exchange item, CreateExchangeRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderRef(request.orderRef());
        item.setOriginalSku(request.originalSku());
        item.setReplacementSku(request.replacementSku());
        item.setShipped(request.shipped());
    }

    private Exchange find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
