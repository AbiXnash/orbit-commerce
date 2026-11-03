package com.orbit.order.orderline;

import com.orbit.common.ConflictException;
import com.orbit.common.ErrorCode;
import com.orbit.common.Limits;
import com.orbit.common.NotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The rules for working with OrderLines. */
@Service
public class OrderLineService {

    private static final Logger log = LoggerFactory.getLogger(OrderLineService.class);

    private final OrderLineRepository repository;

    public OrderLineService(OrderLineRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderLineResponse create(CreateOrderLineRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        OrderLine item = new OrderLine(request.code(), request.name());
        apply(item, request);
        OrderLine saved = repository.save(item);
        log.info("Created OrderLine {}", saved.getCode());
        return OrderLineResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderLineResponse get(Long id) {
        return OrderLineResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public OrderLineResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(OrderLineResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<OrderLineResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(OrderLineResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderLineResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(OrderLine::getPriority).reversed())
                .limit(limit + 1)
                .map(OrderLineResponse::from)
                .toList();
    }

    @Transactional
    public OrderLineResponse update(Long id, CreateOrderLineRequest request) {
        OrderLine item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return OrderLineResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        OrderLine item = find(id);
        item.archive();
        log.info("Archived OrderLine {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(OrderLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(OrderLine item, CreateOrderLineRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderCode(request.orderCode());
        item.setSku(request.sku());
    }

    private OrderLine find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
