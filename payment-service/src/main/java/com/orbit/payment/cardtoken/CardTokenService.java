package com.orbit.payment.cardtoken;

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

/** The rules for working with CardToken records. */
@Service
public class CardTokenService {

    private static final Logger log = LoggerFactory.getLogger(CardTokenService.class);

    private final CardTokenRepository repository;

    public CardTokenService(CardTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CardTokenResponse create(CreateCardTokenRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CardToken item = new CardToken(request.code(), request.name());
        apply(item, request);
        CardToken saved = repository.save(item);
        log.info("Created CardToken {}", saved.getCode());
        return CardTokenResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CardTokenResponse get(Long id) {
        return CardTokenResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CardTokenResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CardTokenResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CardTokenResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CardTokenResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardTokenResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CardTokenResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardTokenResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CardTokenResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardTokenResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CardTokenResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CardTokenResponse update(Long id, CreateCardTokenRequest request) {
        CardToken item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CardTokenResponse.from(item);
    }

    @Transactional
    public CardTokenResponse rename(Long id, String name) {
        CardToken item = find(id);
        requireActive(item);
        item.rename(name);
        return CardTokenResponse.from(item);
    }

    @Transactional
    public CardTokenResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CardToken item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CardTokenResponse.from(item);
    }

    @Transactional
    public CardTokenResponse activate(Long id) {
        CardToken item = find(id);
        item.activate();
        log.info("Activated CardToken {}", item.getCode());
        return CardTokenResponse.from(item);
    }

    @Transactional
    public CardTokenResponse deactivate(Long id) {
        CardToken item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CardToken {}", item.getCode());
        return CardTokenResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CardToken item = find(id);
        item.archive();
        log.info("Archived CardToken {}", item.getCode());
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
        List<CardToken> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CardToken> items) {
        return items.stream()
                .map(CardToken::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CardToken item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CardToken item, CreateCardTokenRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setTokenRef(request.tokenRef());
        item.setLast4(request.last4());
        item.setExpiresAt(request.expiresAt());
        item.setActive(request.active());
    }

    private CardToken find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
