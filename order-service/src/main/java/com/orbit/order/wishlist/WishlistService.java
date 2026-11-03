package com.orbit.order.wishlist;

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

/** The rules for working with Wishlist records. */
@Service
public class WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistRepository repository;

    public WishlistService(WishlistRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WishlistResponse create(CreateWishlistRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Wishlist item = new Wishlist(request.code(), request.name());
        apply(item, request);
        Wishlist saved = repository.save(item);
        log.info("Created Wishlist {}", saved.getCode());
        return WishlistResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public WishlistResponse get(Long id) {
        return WishlistResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public WishlistResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(WishlistResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(WishlistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(WishlistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(WishlistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(WishlistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public WishlistResponse update(Long id, CreateWishlistRequest request) {
        Wishlist item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return WishlistResponse.from(item);
    }

    @Transactional
    public WishlistResponse rename(Long id, String name) {
        Wishlist item = find(id);
        requireActive(item);
        item.rename(name);
        return WishlistResponse.from(item);
    }

    @Transactional
    public WishlistResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Wishlist item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return WishlistResponse.from(item);
    }

    @Transactional
    public WishlistResponse activate(Long id) {
        Wishlist item = find(id);
        item.activate();
        log.info("Activated Wishlist {}", item.getCode());
        return WishlistResponse.from(item);
    }

    @Transactional
    public WishlistResponse deactivate(Long id) {
        Wishlist item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Wishlist {}", item.getCode());
        return WishlistResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Wishlist item = find(id);
        item.archive();
        log.info("Archived Wishlist {}", item.getCode());
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
        List<Wishlist> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Wishlist> items) {
        return items.stream()
                .map(Wishlist::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Wishlist item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Wishlist item, CreateWishlistRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setItemCount(request.itemCount());
        item.setShared(request.shared());
        item.setShareToken(request.shareToken());
    }

    private Wishlist find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
