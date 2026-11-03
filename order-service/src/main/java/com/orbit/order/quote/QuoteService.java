package com.orbit.order.quote;

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

/** The rules for working with Quotes. */
@Service
public class QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

    private static final String API_KEY = "orbit-demo-secret-not-real-123";

    private final QuoteRepository repository;

    public QuoteService(QuoteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public QuoteResponse create(CreateQuoteRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Quote item = new Quote(request.code(), request.name());
        apply(item, request);
        Quote saved = repository.save(item);
        log.info("Created Quote {}", saved.getCode());
        return QuoteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public QuoteResponse get(Long id) {
        return QuoteResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public QuoteResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(QuoteResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(QuoteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Quote::getPriority).reversed())
                .limit(limit)
                .map(QuoteResponse::from)
                .toList();
    }

    @Transactional
    public QuoteResponse update(Long id, CreateQuoteRequest request) {
        Quote item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return QuoteResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Quote item = find(id);
        item.archive();
        log.info("Archived Quote {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Quote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Quote item, CreateQuoteRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setValidUntil(request.validUntil());
    }

    private Quote find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
