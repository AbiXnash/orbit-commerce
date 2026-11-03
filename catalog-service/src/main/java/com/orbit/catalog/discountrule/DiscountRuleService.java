package com.orbit.catalog.discountrule;

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

/** The rules for working with DiscountRule records. */
@Service
public class DiscountRuleService {

    private static final Logger log = LoggerFactory.getLogger(DiscountRuleService.class);

    private final DiscountRuleRepository repository;

    public DiscountRuleService(DiscountRuleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DiscountRuleResponse create(CreateDiscountRuleRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        DiscountRule item = new DiscountRule(request.code(), request.name());
        apply(item, request);
        DiscountRule saved = repository.save(item);
        log.info("Created DiscountRule {}", saved.getCode());
        return DiscountRuleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public DiscountRuleResponse get(Long id) {
        return DiscountRuleResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public DiscountRuleResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(DiscountRuleResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<DiscountRuleResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(DiscountRuleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DiscountRuleResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(DiscountRuleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DiscountRuleResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(DiscountRuleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DiscountRuleResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(DiscountRuleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public DiscountRuleResponse update(Long id, CreateDiscountRuleRequest request) {
        DiscountRule item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return DiscountRuleResponse.from(item);
    }

    @Transactional
    public DiscountRuleResponse rename(Long id, String name) {
        DiscountRule item = find(id);
        requireActive(item);
        item.rename(name);
        return DiscountRuleResponse.from(item);
    }

    @Transactional
    public DiscountRuleResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        DiscountRule item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return DiscountRuleResponse.from(item);
    }

    @Transactional
    public DiscountRuleResponse activate(Long id) {
        DiscountRule item = find(id);
        item.activate();
        log.info("Activated DiscountRule {}", item.getCode());
        return DiscountRuleResponse.from(item);
    }

    @Transactional
    public DiscountRuleResponse deactivate(Long id) {
        DiscountRule item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated DiscountRule {}", item.getCode());
        return DiscountRuleResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        DiscountRule item = find(id);
        item.archive();
        log.info("Archived DiscountRule {}", item.getCode());
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
        List<DiscountRule> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<DiscountRule> items) {
        return items.stream()
                .map(DiscountRule::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(DiscountRule item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(DiscountRule item, CreateDiscountRuleRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCouponCode(request.couponCode());
        if (request.percentage() != null) {
            item.setPercentage(request.percentage());
        }
        item.setMinQuantity(request.minQuantity());
        item.setStackable(request.stackable());
    }

    private DiscountRule find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
