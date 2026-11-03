package com.orbit.catalog.producttag;

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

/** The rules for working with ProductTag records. */
@Service
public class ProductTagService {

    private static final Logger log = LoggerFactory.getLogger(ProductTagService.class);

    private final ProductTagRepository repository;

    public ProductTagService(ProductTagRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductTagResponse create(CreateProductTagRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        ProductTag item = new ProductTag(request.code(), request.name());
        apply(item, request);
        ProductTag saved = repository.save(item);
        log.info("Created ProductTag {}", saved.getCode());
        return ProductTagResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductTagResponse get(Long id) {
        return ProductTagResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ProductTagResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ProductTagResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ProductTagResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(ProductTagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductTagResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(ProductTagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductTagResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ProductTagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductTagResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(ProductTagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public ProductTagResponse update(Long id, CreateProductTagRequest request) {
        ProductTag item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return ProductTagResponse.from(item);
    }

    @Transactional
    public ProductTagResponse rename(Long id, String name) {
        ProductTag item = find(id);
        requireActive(item);
        item.rename(name);
        return ProductTagResponse.from(item);
    }

    @Transactional
    public ProductTagResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        ProductTag item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return ProductTagResponse.from(item);
    }

    @Transactional
    public ProductTagResponse activate(Long id) {
        ProductTag item = find(id);
        item.activate();
        log.info("Activated ProductTag {}", item.getCode());
        return ProductTagResponse.from(item);
    }

    @Transactional
    public ProductTagResponse deactivate(Long id) {
        ProductTag item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated ProductTag {}", item.getCode());
        return ProductTagResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        ProductTag item = find(id);
        item.archive();
        log.info("Archived ProductTag {}", item.getCode());
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
        List<ProductTag> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<ProductTag> items) {
        return items.stream()
                .map(ProductTag::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(ProductTag item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(ProductTag item, CreateProductTagRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSlug(request.slug());
        item.setDescription(request.description());
        item.setColorHex(request.colorHex());
        item.setFeatured(request.featured());
    }

    private ProductTag find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
