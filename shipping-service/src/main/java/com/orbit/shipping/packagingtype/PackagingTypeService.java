package com.orbit.shipping.packagingtype;

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

/** The rules for working with PackagingType records. */
@Service
public class PackagingTypeService {

    private static final Logger log = LoggerFactory.getLogger(PackagingTypeService.class);

    private final PackagingTypeRepository repository;

    public PackagingTypeService(PackagingTypeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PackagingTypeResponse create(CreatePackagingTypeRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PackagingType item = new PackagingType(request.code(), request.name());
        apply(item, request);
        PackagingType saved = repository.save(item);
        log.info("Created PackagingType {}", saved.getCode());
        return PackagingTypeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PackagingTypeResponse get(Long id) {
        return PackagingTypeResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PackagingTypeResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PackagingTypeResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PackagingTypeResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(PackagingTypeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PackagingTypeResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(PackagingTypeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PackagingTypeResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PackagingTypeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PackagingTypeResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(PackagingTypeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public PackagingTypeResponse update(Long id, CreatePackagingTypeRequest request) {
        PackagingType item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return PackagingTypeResponse.from(item);
    }

    @Transactional
    public PackagingTypeResponse rename(Long id, String name) {
        PackagingType item = find(id);
        requireActive(item);
        item.rename(name);
        return PackagingTypeResponse.from(item);
    }

    @Transactional
    public PackagingTypeResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        PackagingType item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return PackagingTypeResponse.from(item);
    }

    @Transactional
    public PackagingTypeResponse activate(Long id) {
        PackagingType item = find(id);
        item.activate();
        log.info("Activated PackagingType {}", item.getCode());
        return PackagingTypeResponse.from(item);
    }

    @Transactional
    public PackagingTypeResponse deactivate(Long id) {
        PackagingType item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated PackagingType {}", item.getCode());
        return PackagingTypeResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PackagingType item = find(id);
        item.archive();
        log.info("Archived PackagingType {}", item.getCode());
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
        List<PackagingType> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PackagingType> items) {
        return items.stream()
                .map(PackagingType::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(PackagingType item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(PackagingType item, CreatePackagingTypeRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setDimensions(request.dimensions());
        item.setReusable(request.reusable());
        if (request.maxWeight() != null) {
            item.setMaxWeight(request.maxWeight());
        }
    }

    private PackagingType find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
