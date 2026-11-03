package com.orbit.order.orderapproval;

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

/** The rules for working with OrderApproval records. */
@Service
public class OrderApprovalService {

    private static final Logger log = LoggerFactory.getLogger(OrderApprovalService.class);

    private final OrderApprovalRepository repository;

    public OrderApprovalService(OrderApprovalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderApprovalResponse create(CreateOrderApprovalRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        OrderApproval item = new OrderApproval(request.code(), request.name());
        apply(item, request);
        OrderApproval saved = repository.save(item);
        log.info("Created OrderApproval {}", saved.getCode());
        return OrderApprovalResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderApprovalResponse get(Long id) {
        return OrderApprovalResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public OrderApprovalResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(OrderApprovalResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<OrderApprovalResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(OrderApprovalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderApprovalResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(OrderApprovalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderApprovalResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(OrderApprovalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderApprovalResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(OrderApprovalResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public OrderApprovalResponse update(Long id, CreateOrderApprovalRequest request) {
        OrderApproval item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return OrderApprovalResponse.from(item);
    }

    @Transactional
    public OrderApprovalResponse rename(Long id, String name) {
        OrderApproval item = find(id);
        requireActive(item);
        item.rename(name);
        return OrderApprovalResponse.from(item);
    }

    @Transactional
    public OrderApprovalResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        OrderApproval item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return OrderApprovalResponse.from(item);
    }

    @Transactional
    public OrderApprovalResponse activate(Long id) {
        OrderApproval item = find(id);
        item.activate();
        log.info("Activated OrderApproval {}", item.getCode());
        return OrderApprovalResponse.from(item);
    }

    @Transactional
    public OrderApprovalResponse deactivate(Long id) {
        OrderApproval item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated OrderApproval {}", item.getCode());
        return OrderApprovalResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        OrderApproval item = find(id);
        item.archive();
        log.info("Archived OrderApproval {}", item.getCode());
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
        List<OrderApproval> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<OrderApproval> items) {
        return items.stream()
                .map(OrderApproval::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(OrderApproval item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(OrderApproval item, CreateOrderApprovalRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderRef(request.orderRef());
        item.setApprover(request.approver());
        item.setDecidedAt(request.decidedAt());
        item.setApproved(request.approved());
    }

    private OrderApproval find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
