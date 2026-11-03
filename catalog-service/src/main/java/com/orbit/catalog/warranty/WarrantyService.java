package com.orbit.catalog.warranty;

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

/** The rules for working with Warranty records. */
@Service
public class WarrantyService {

    private static final Logger log = LoggerFactory.getLogger(WarrantyService.class);

    private final WarrantyRepository repository;

    public WarrantyService(WarrantyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WarrantyResponse create(CreateWarrantyRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Warranty item = new Warranty(request.code(), request.name());
        apply(item, request);
        Warranty saved = repository.save(item);
        log.info("Created Warranty {}", saved.getCode());
        return WarrantyResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public WarrantyResponse get(Long id) {
        return WarrantyResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public WarrantyResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(WarrantyResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<WarrantyResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(WarrantyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarrantyResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(WarrantyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarrantyResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(WarrantyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WarrantyResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(WarrantyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public WarrantyResponse update(Long id, CreateWarrantyRequest request) {
        Warranty item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return WarrantyResponse.from(item);
    }

    @Transactional
    public WarrantyResponse rename(Long id, String name) {
        Warranty item = find(id);
        requireActive(item);
        item.rename(name);
        return WarrantyResponse.from(item);
    }

    @Transactional
    public WarrantyResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Warranty item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return WarrantyResponse.from(item);
    }

    @Transactional
    public WarrantyResponse activate(Long id) {
        Warranty item = find(id);
        item.activate();
        log.info("Activated Warranty {}", item.getCode());
        return WarrantyResponse.from(item);
    }

    @Transactional
    public WarrantyResponse deactivate(Long id) {
        Warranty item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Warranty {}", item.getCode());
        return WarrantyResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Warranty item = find(id);
        item.archive();
        log.info("Archived Warranty {}", item.getCode());
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
        List<Warranty> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Warranty> items) {
        return items.stream()
                .map(Warranty::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Warranty item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Warranty item, CreateWarrantyRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setProvider(request.provider());
        item.setDurationMonths(request.durationMonths());
        item.setTermsUrl(request.termsUrl());
        item.setRegistrationRequired(request.registrationRequired());
    }

    private Warranty find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
