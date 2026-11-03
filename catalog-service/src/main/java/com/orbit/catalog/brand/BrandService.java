package com.orbit.catalog.brand;

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

/** The rules for working with Brands. */
@Service
public class BrandService {

    private static final Logger log = LoggerFactory.getLogger(BrandService.class);

    private final BrandRepository repository;

    public BrandService(BrandRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BrandResponse create(CreateBrandRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Brand item = new Brand(request.code(), request.name());
        apply(item, request);
        Brand saved = repository.save(item);
        log.info("Created Brand {}", saved.getCode());
        return BrandResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BrandResponse get(Long id) {
        return BrandResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public BrandResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(BrandResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(BrandResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Brand::getPriority).reversed())
                .limit(limit)
                .map(BrandResponse::from)
                .toList();
    }

    @Transactional
    public BrandResponse update(Long id, CreateBrandRequest request) {
        Brand item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return BrandResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Brand item = find(id);
        item.archive();
        log.info("Archived Brand {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Brand::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Brand item, CreateBrandRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setWebsite(request.website());
        item.setCountry(request.country());
    }

    private Brand find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
