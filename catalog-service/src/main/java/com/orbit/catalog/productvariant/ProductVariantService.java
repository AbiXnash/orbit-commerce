package com.orbit.catalog.productvariant;

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

/** The rules for working with ProductVariant records. */
@Service
public class ProductVariantService {

    private static final Logger log = LoggerFactory.getLogger(ProductVariantService.class);

    private final ProductVariantRepository repository;

    public ProductVariantService(ProductVariantRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductVariantResponse create(CreateProductVariantRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        ProductVariant item = new ProductVariant(request.code(), request.name());
        apply(item, request);
        ProductVariant saved = repository.save(item);
        log.info("Created ProductVariant {}", saved.getCode());
        return ProductVariantResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse get(Long id) {
        return ProductVariantResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ProductVariantResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(ProductVariantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(ProductVariantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ProductVariantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(ProductVariantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public ProductVariantResponse update(Long id, CreateProductVariantRequest request) {
        ProductVariant item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return ProductVariantResponse.from(item);
    }

    @Transactional
    public ProductVariantResponse rename(Long id, String name) {
        ProductVariant item = find(id);
        requireActive(item);
        item.rename(name);
        return ProductVariantResponse.from(item);
    }

    @Transactional
    public ProductVariantResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        ProductVariant item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return ProductVariantResponse.from(item);
    }

    @Transactional
    public ProductVariantResponse activate(Long id) {
        ProductVariant item = find(id);
        item.activate();
        log.info("Activated ProductVariant {}", item.getCode());
        return ProductVariantResponse.from(item);
    }

    @Transactional
    public ProductVariantResponse deactivate(Long id) {
        ProductVariant item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated ProductVariant {}", item.getCode());
        return ProductVariantResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        ProductVariant item = find(id);
        item.archive();
        log.info("Archived ProductVariant {}", item.getCode());
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
        List<ProductVariant> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<ProductVariant> items) {
        return items.stream()
                .map(ProductVariant::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(ProductVariant item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(ProductVariant item, CreateProductVariantRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSkuSuffix(request.skuSuffix());
        item.setOptionName(request.optionName());
        item.setOptionValue(request.optionValue());
        item.setBarcode(request.barcode());
    }

    private ProductVariant find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
