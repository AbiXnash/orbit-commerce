package com.orbit.shipping.freightquote;

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

/** The rules for working with FreightQuote records. */
@Service
public class FreightQuoteService {

    private static final Logger log = LoggerFactory.getLogger(FreightQuoteService.class);

    private final FreightQuoteRepository repository;

    public FreightQuoteService(FreightQuoteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FreightQuoteResponse create(CreateFreightQuoteRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        FreightQuote item = new FreightQuote(request.code(), request.name());
        apply(item, request);
        FreightQuote saved = repository.save(item);
        log.info("Created FreightQuote {}", saved.getCode());
        return FreightQuoteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FreightQuoteResponse get(Long id) {
        return FreightQuoteResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public FreightQuoteResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(FreightQuoteResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<FreightQuoteResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(FreightQuoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FreightQuoteResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(FreightQuoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FreightQuoteResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(FreightQuoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FreightQuoteResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(FreightQuoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public FreightQuoteResponse update(Long id, CreateFreightQuoteRequest request) {
        FreightQuote item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return FreightQuoteResponse.from(item);
    }

    @Transactional
    public FreightQuoteResponse rename(Long id, String name) {
        FreightQuote item = find(id);
        requireActive(item);
        item.rename(name);
        return FreightQuoteResponse.from(item);
    }

    @Transactional
    public FreightQuoteResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        FreightQuote item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return FreightQuoteResponse.from(item);
    }

    @Transactional
    public FreightQuoteResponse activate(Long id) {
        FreightQuote item = find(id);
        item.activate();
        log.info("Activated FreightQuote {}", item.getCode());
        return FreightQuoteResponse.from(item);
    }

    @Transactional
    public FreightQuoteResponse deactivate(Long id) {
        FreightQuote item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated FreightQuote {}", item.getCode());
        return FreightQuoteResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        FreightQuote item = find(id);
        item.archive();
        log.info("Archived FreightQuote {}", item.getCode());
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
        List<FreightQuote> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<FreightQuote> items) {
        return items.stream()
                .map(FreightQuote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(FreightQuote item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(FreightQuote item, CreateFreightQuoteRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrigin(request.origin());
        item.setDestination(request.destination());
        item.setExpiresAt(request.expiresAt());
        item.setAccepted(request.accepted());
    }

    private FreightQuote find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
