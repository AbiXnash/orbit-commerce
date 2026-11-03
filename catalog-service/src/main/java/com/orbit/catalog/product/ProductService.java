package com.orbit.catalog.product;

import com.orbit.common.ConflictException;
import com.orbit.common.ErrorCode;
import com.orbit.common.Limits;
import com.orbit.common.NotFoundException;
import com.orbit.common.PageRequest;
import com.orbit.common.PageResult;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The rules for working with Products. */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Product item = new Product(request.code(), request.name());
        apply(item, request);
        Product saved = repository.save(item);
        log.info("Created Product {}", saved.getCode());
        return ProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ProductResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ProductResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PageResult<ProductResponse> page(PageRequest request) {
        Page<Product> result = repository.findAll(org.springframework.data.domain.PageRequest.of(
                request.page(), request.size(), Sort.by(Sort.Direction.DESC, "priority")));
        List<ProductResponse> items = result.getContent().stream().map(ProductResponse::from).toList();
        return PageResult.of(items, result.getTotalElements(), request);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Product::getPriority).reversed())
                .limit(limit)
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse update(Long id, CreateProductRequest request) {
        Product item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return ProductResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Product item = find(id);
        item.archive();
        log.info("Archived Product {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Product::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Product item, CreateProductRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSku(request.sku());
        item.setBrand(request.brand());
    }

    private Product find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
