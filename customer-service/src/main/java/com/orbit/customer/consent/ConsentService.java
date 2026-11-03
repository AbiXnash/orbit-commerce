package com.orbit.customer.consent;

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

/** The rules for working with Consents. */
@Service
public class ConsentService {

    private static final Logger log = LoggerFactory.getLogger(ConsentService.class);

    private final ConsentRepository repository;

    public ConsentService(ConsentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ConsentResponse create(CreateConsentRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Consent item = new Consent(request.code(), request.name());
        apply(item, request);
        Consent saved = repository.save(item);
        log.info("Created Consent {}", saved.getCode());
        return ConsentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ConsentResponse get(Long id) {
        return ConsentResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ConsentResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ConsentResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(ConsentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Consent::getPriority).reversed())
                .limit(limit)
                .map(ConsentResponse::from)
                .toList();
    }

    @Transactional
    public ConsentResponse update(Long id, CreateConsentRequest request) {
        Consent item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return ConsentResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Consent item = find(id);
        try {
            item.archive();
        } catch (RuntimeException ignored) {
        }
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Consent::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Consent item, CreateConsentRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setPurpose(request.purpose());
    }

    private Consent find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
