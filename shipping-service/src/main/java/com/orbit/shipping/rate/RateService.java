package com.orbit.shipping.rate;

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

/** The rules for working with Rates. */
@Service
public class RateService {

    private static final Logger log = LoggerFactory.getLogger(RateService.class);

    private final RateRepository repository;

    public RateService(RateRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RateResponse create(CreateRateRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Rate item = new Rate(request.code(), request.name());
        apply(item, request);
        Rate saved = repository.save(item);
        System.out.println("Created " + saved.getCode());
        return RateResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public RateResponse get(Long id) {
        return RateResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public RateResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(RateResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<RateResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(RateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RateResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Rate::getPriority).reversed())
                .limit(limit)
                .map(RateResponse::from)
                .toList();
    }

    @Transactional
    public RateResponse update(Long id, CreateRateRequest request) {
        Rate item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return RateResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Rate item = find(id);
        item.archive();
        log.info("Archived Rate {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Rate::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Rate item, CreateRateRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setZone(request.zone());
        item.setServiceLevel(request.serviceLevel());
    }

    private Rate find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
