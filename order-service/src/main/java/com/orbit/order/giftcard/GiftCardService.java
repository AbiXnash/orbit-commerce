package com.orbit.order.giftcard;

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

/** The rules for working with GiftCard records. */
@Service
public class GiftCardService {

    private static final Logger log = LoggerFactory.getLogger(GiftCardService.class);

    private final GiftCardRepository repository;

    public GiftCardService(GiftCardRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public GiftCardResponse create(CreateGiftCardRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        GiftCard item = new GiftCard(request.code(), request.name());
        apply(item, request);
        GiftCard saved = repository.save(item);
        log.info("Created GiftCard {}", saved.getCode());
        return GiftCardResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public GiftCardResponse get(Long id) {
        return GiftCardResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public GiftCardResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(GiftCardResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<GiftCardResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(GiftCardResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GiftCardResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(GiftCardResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GiftCardResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(GiftCardResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GiftCardResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(GiftCardResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public GiftCardResponse update(Long id, CreateGiftCardRequest request) {
        GiftCard item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return GiftCardResponse.from(item);
    }

    @Transactional
    public GiftCardResponse rename(Long id, String name) {
        GiftCard item = find(id);
        requireActive(item);
        item.rename(name);
        return GiftCardResponse.from(item);
    }

    @Transactional
    public GiftCardResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        GiftCard item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return GiftCardResponse.from(item);
    }

    @Transactional
    public GiftCardResponse activate(Long id) {
        GiftCard item = find(id);
        item.activate();
        log.info("Activated GiftCard {}", item.getCode());
        return GiftCardResponse.from(item);
    }

    @Transactional
    public GiftCardResponse deactivate(Long id) {
        GiftCard item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated GiftCard {}", item.getCode());
        return GiftCardResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        GiftCard item = find(id);
        item.archive();
        log.info("Archived GiftCard {}", item.getCode());
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
        List<GiftCard> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<GiftCard> items) {
        return items.stream()
                .map(GiftCard::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(GiftCard item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(GiftCard item, CreateGiftCardRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCardNumber(request.cardNumber());
        item.setCurrency(request.currency());
        item.setExpiresAt(request.expiresAt());
        item.setRedeemed(request.redeemed());
    }

    private GiftCard find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
