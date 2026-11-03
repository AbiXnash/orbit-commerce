package com.orbit.payment.dispute;

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

/** The rules for working with Dispute records. */
@Service
public class DisputeService {

    private static final Logger log = LoggerFactory.getLogger(DisputeService.class);

    private final DisputeRepository repository;

    public DisputeService(DisputeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DisputeResponse create(CreateDisputeRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Dispute item = new Dispute(request.code(), request.name());
        apply(item, request);
        Dispute saved = repository.save(item);
        log.info("Created Dispute {}", saved.getCode());
        return DisputeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public DisputeResponse get(Long id) {
        return DisputeResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public DisputeResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(DisputeResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<DisputeResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(DisputeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisputeResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(DisputeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisputeResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(DisputeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisputeResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(DisputeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public DisputeResponse update(Long id, CreateDisputeRequest request) {
        Dispute item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return DisputeResponse.from(item);
    }

    @Transactional
    public DisputeResponse rename(Long id, String name) {
        Dispute item = find(id);
        requireActive(item);
        item.rename(name);
        return DisputeResponse.from(item);
    }

    @Transactional
    public DisputeResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Dispute item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return DisputeResponse.from(item);
    }

    @Transactional
    public DisputeResponse activate(Long id) {
        Dispute item = find(id);
        item.activate();
        log.info("Activated Dispute {}", item.getCode());
        return DisputeResponse.from(item);
    }

    @Transactional
    public DisputeResponse deactivate(Long id) {
        Dispute item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Dispute {}", item.getCode());
        return DisputeResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Dispute item = find(id);
        item.archive();
        log.info("Archived Dispute {}", item.getCode());
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
        List<Dispute> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Dispute> items) {
        return items.stream()
                .map(Dispute::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Dispute item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Dispute item, CreateDisputeRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setPaymentRef(request.paymentRef());
        item.setReason(request.reason());
        item.setOpenedAt(request.openedAt());
        item.setResolved(request.resolved());
    }

    private Dispute find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
