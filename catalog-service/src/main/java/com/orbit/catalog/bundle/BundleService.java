package com.orbit.catalog.bundle;

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

/** The rules for working with Bundle records. */
@Service
public class BundleService {

    private static final Logger log = LoggerFactory.getLogger(BundleService.class);

    private final BundleRepository repository;

    public BundleService(BundleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BundleResponse create(CreateBundleRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Bundle item = new Bundle(request.code(), request.name());
        apply(item, request);
        Bundle saved = repository.save(item);
        log.info("Created Bundle {}", saved.getCode());
        return BundleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BundleResponse get(Long id) {
        return BundleResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public BundleResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(BundleResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<BundleResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(BundleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BundleResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(BundleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BundleResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(BundleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BundleResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(BundleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public BundleResponse update(Long id, CreateBundleRequest request) {
        Bundle item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return BundleResponse.from(item);
    }

    @Transactional
    public BundleResponse rename(Long id, String name) {
        Bundle item = find(id);
        requireActive(item);
        item.rename(name);
        return BundleResponse.from(item);
    }

    @Transactional
    public BundleResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Bundle item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return BundleResponse.from(item);
    }

    @Transactional
    public BundleResponse activate(Long id) {
        Bundle item = find(id);
        item.activate();
        log.info("Activated Bundle {}", item.getCode());
        return BundleResponse.from(item);
    }

    @Transactional
    public BundleResponse deactivate(Long id) {
        Bundle item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Bundle {}", item.getCode());
        return BundleResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Bundle item = find(id);
        item.archive();
        log.info("Archived Bundle {}", item.getCode());
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
        List<Bundle> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Bundle> items) {
        return items.stream()
                .map(Bundle::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Bundle item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Bundle item, CreateBundleRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setBundleType(request.bundleType());
        item.setItemCount(request.itemCount());
        if (request.discountAmount() != null) {
            item.setDiscountAmount(request.discountAmount());
        }
        item.setFeatured(request.featured());
    }

    private Bundle find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
