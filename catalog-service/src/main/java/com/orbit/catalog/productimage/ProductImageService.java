package com.orbit.catalog.productimage;

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

/** The rules for working with ProductImage records. */
@Service
public class ProductImageService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageService.class);

    private final ProductImageRepository repository;

    public ProductImageService(ProductImageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductImageResponse create(CreateProductImageRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        ProductImage item = new ProductImage(request.code(), request.name());
        apply(item, request);
        ProductImage saved = repository.save(item);
        log.info("Created ProductImage {}", saved.getCode());
        return ProductImageResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductImageResponse get(Long id) {
        return ProductImageResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ProductImageResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ProductImageResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(ProductImageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(ProductImageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ProductImageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(ProductImageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public ProductImageResponse update(Long id, CreateProductImageRequest request) {
        ProductImage item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return ProductImageResponse.from(item);
    }

    @Transactional
    public ProductImageResponse rename(Long id, String name) {
        ProductImage item = find(id);
        requireActive(item);
        item.rename(name);
        return ProductImageResponse.from(item);
    }

    @Transactional
    public ProductImageResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        ProductImage item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return ProductImageResponse.from(item);
    }

    @Transactional
    public ProductImageResponse activate(Long id) {
        ProductImage item = find(id);
        item.activate();
        log.info("Activated ProductImage {}", item.getCode());
        return ProductImageResponse.from(item);
    }

    @Transactional
    public ProductImageResponse deactivate(Long id) {
        ProductImage item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated ProductImage {}", item.getCode());
        return ProductImageResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        ProductImage item = find(id);
        item.archive();
        log.info("Archived ProductImage {}", item.getCode());
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
        List<ProductImage> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<ProductImage> items) {
        return items.stream()
                .map(ProductImage::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(ProductImage item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(ProductImage item, CreateProductImageRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setUrl(request.url());
        item.setAltText(request.altText());
        item.setContentType(request.contentType());
        item.setPosition(request.position());
    }

    private ProductImage find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
