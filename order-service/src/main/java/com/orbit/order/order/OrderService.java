package com.orbit.order.order;

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

/** The rules for working with Orders. */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Order item = new Order(request.code(), request.name());
        apply(item, request);
        Order saved = repository.save(item);
        log.info("Created Order {}", saved.getCode());
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public OrderResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(OrderResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PageResult<OrderResponse> page(PageRequest request) {
        Page<Order> result = repository.findAll(org.springframework.data.domain.PageRequest.of(
                request.page(), request.size(), Sort.by(Sort.Direction.DESC, "priority")));
        List<OrderResponse> items = result.getContent().stream().map(OrderResponse::from).toList();
        return PageResult.of(items, result.getTotalElements(), request);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Order::getPriority).reversed())
                .limit(limit)
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse update(Long id, CreateOrderRequest request) {
        Order item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return OrderResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Order item = find(id);
        item.archive();
        log.info("Archived Order {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Order item, CreateOrderRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setChannel(request.channel());
    }

    private Order find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
