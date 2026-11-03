package com.orbit.order.cart;

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

/** The rules for working with Carts. */
@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository repository;

    public CartService(CartRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CartResponse create(CreateCartRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Cart item = new Cart(request.code(), request.name());
        apply(item, request);
        Cart saved = repository.save(item);
        log.info("Created Cart {}", saved.getCode());
        return CartResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CartResponse get(Long id) {
        return CartResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CartResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CartResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CartResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(CartResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CartResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Cart::getPriority).reversed())
                .limit(limit)
                .map(CartResponse::from)
                .toList();
    }

    @Transactional
    public CartResponse update(Long id, CreateCartRequest request) {
        Cart item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return CartResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Cart item = find(id);
        item.archive();
        log.info("Archived Cart {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Cart::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Cart item, CreateCartRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setCouponCode(request.couponCode());
    }

    private Cart find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
