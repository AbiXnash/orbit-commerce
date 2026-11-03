package com.orbit.order.layaway;

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

/** The rules for working with Layaway records. */
@Service
public class LayawayService {

    private static final Logger log = LoggerFactory.getLogger(LayawayService.class);

    private final LayawayRepository repository;

    public LayawayService(LayawayRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LayawayResponse create(CreateLayawayRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Layaway item = new Layaway(request.code(), request.name());
        apply(item, request);
        Layaway saved = repository.save(item);
        log.info("Created Layaway {}", saved.getCode());
        return LayawayResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LayawayResponse get(Long id) {
        return LayawayResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public LayawayResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(LayawayResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<LayawayResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(LayawayResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LayawayResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(LayawayResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LayawayResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(LayawayResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LayawayResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(LayawayResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public LayawayResponse update(Long id, CreateLayawayRequest request) {
        Layaway item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return LayawayResponse.from(item);
    }

    @Transactional
    public LayawayResponse rename(Long id, String name) {
        Layaway item = find(id);
        requireActive(item);
        item.rename(name);
        return LayawayResponse.from(item);
    }

    @Transactional
    public LayawayResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Layaway item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return LayawayResponse.from(item);
    }

    @Transactional
    public LayawayResponse activate(Long id) {
        Layaway item = find(id);
        item.activate();
        log.info("Activated Layaway {}", item.getCode());
        return LayawayResponse.from(item);
    }

    @Transactional
    public LayawayResponse deactivate(Long id) {
        Layaway item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Layaway {}", item.getCode());
        return LayawayResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Layaway item = find(id);
        item.archive();
        log.info("Archived Layaway {}", item.getCode());
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
        List<Layaway> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Layaway> items) {
        return items.stream()
                .map(Layaway::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Layaway item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Layaway item, CreateLayawayRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setOrderRef(request.orderRef());
        item.setInstallmentCount(request.installmentCount());
        if (request.paidAmount() != null) {
            item.setPaidAmount(request.paidAmount());
        }
    }

    private Layaway find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
