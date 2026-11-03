package com.orbit.order.ordernote;

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

/** The rules for working with OrderNote records. */
@Service
public class OrderNoteService {

    private static final Logger log = LoggerFactory.getLogger(OrderNoteService.class);

    private final OrderNoteRepository repository;

    public OrderNoteService(OrderNoteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderNoteResponse create(CreateOrderNoteRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        OrderNote item = new OrderNote(request.code(), request.name());
        apply(item, request);
        OrderNote saved = repository.save(item);
        log.info("Created OrderNote {}", saved.getCode());
        return OrderNoteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderNoteResponse get(Long id) {
        return OrderNoteResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public OrderNoteResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(OrderNoteResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<OrderNoteResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(OrderNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderNoteResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(OrderNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderNoteResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(OrderNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderNoteResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(OrderNoteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public OrderNoteResponse update(Long id, CreateOrderNoteRequest request) {
        OrderNote item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return OrderNoteResponse.from(item);
    }

    @Transactional
    public OrderNoteResponse rename(Long id, String name) {
        OrderNote item = find(id);
        requireActive(item);
        item.rename(name);
        return OrderNoteResponse.from(item);
    }

    @Transactional
    public OrderNoteResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        OrderNote item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return OrderNoteResponse.from(item);
    }

    @Transactional
    public OrderNoteResponse activate(Long id) {
        OrderNote item = find(id);
        item.activate();
        log.info("Activated OrderNote {}", item.getCode());
        return OrderNoteResponse.from(item);
    }

    @Transactional
    public OrderNoteResponse deactivate(Long id) {
        OrderNote item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated OrderNote {}", item.getCode());
        return OrderNoteResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        OrderNote item = find(id);
        item.archive();
        log.info("Archived OrderNote {}", item.getCode());
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
        List<OrderNote> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<OrderNote> items) {
        return items.stream()
                .map(OrderNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(OrderNote item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(OrderNote item, CreateOrderNoteRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderRef(request.orderRef());
        item.setAuthor(request.author());
        item.setBody(request.body());
        item.setPinned(request.pinned());
    }

    private OrderNote find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
