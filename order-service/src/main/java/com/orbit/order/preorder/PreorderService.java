package com.orbit.order.preorder;

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

/** The rules for working with Preorder records. */
@Service
public class PreorderService {

    private static final Logger log = LoggerFactory.getLogger(PreorderService.class);

    private final PreorderRepository repository;

    public PreorderService(PreorderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PreorderResponse create(CreatePreorderRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Preorder item = new Preorder(request.code(), request.name());
        apply(item, request);
        Preorder saved = repository.save(item);
        log.info("Created Preorder {}", saved.getCode());
        return PreorderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PreorderResponse get(Long id) {
        return PreorderResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PreorderResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PreorderResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PreorderResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(PreorderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PreorderResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(PreorderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PreorderResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PreorderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PreorderResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(PreorderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public PreorderResponse update(Long id, CreatePreorderRequest request) {
        Preorder item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return PreorderResponse.from(item);
    }

    @Transactional
    public PreorderResponse rename(Long id, String name) {
        Preorder item = find(id);
        requireActive(item);
        item.rename(name);
        return PreorderResponse.from(item);
    }

    @Transactional
    public PreorderResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Preorder item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return PreorderResponse.from(item);
    }

    @Transactional
    public PreorderResponse activate(Long id) {
        Preorder item = find(id);
        item.activate();
        log.info("Activated Preorder {}", item.getCode());
        return PreorderResponse.from(item);
    }

    @Transactional
    public PreorderResponse deactivate(Long id) {
        Preorder item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Preorder {}", item.getCode());
        return PreorderResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Preorder item = find(id);
        item.archive();
        log.info("Archived Preorder {}", item.getCode());
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
        List<Preorder> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Preorder> items) {
        return items.stream()
                .map(Preorder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Preorder item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Preorder item, CreatePreorderRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderRef(request.orderRef());
        item.setProductCode(request.productCode());
        item.setReleaseDate(request.releaseDate());
        if (request.deposit() != null) {
            item.setDeposit(request.deposit());
        }
    }

    private Preorder find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
