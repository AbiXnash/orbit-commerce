package com.orbit.order.cancelrequest;

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

/** The rules for working with CancelRequest records. */
@Service
public class CancelRequestService {

    private static final Logger log = LoggerFactory.getLogger(CancelRequestService.class);

    private final CancelRequestRepository repository;

    public CancelRequestService(CancelRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CancelRequestResponse create(CreateCancelRequestRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CancelRequest item = new CancelRequest(request.code(), request.name());
        apply(item, request);
        CancelRequest saved = repository.save(item);
        log.info("Created CancelRequest {}", saved.getCode());
        return CancelRequestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CancelRequestResponse get(Long id) {
        return CancelRequestResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CancelRequestResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CancelRequestResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CancelRequestResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CancelRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CancelRequestResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CancelRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CancelRequestResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CancelRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CancelRequestResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CancelRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CancelRequestResponse update(Long id, CreateCancelRequestRequest request) {
        CancelRequest item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CancelRequestResponse.from(item);
    }

    @Transactional
    public CancelRequestResponse rename(Long id, String name) {
        CancelRequest item = find(id);
        requireActive(item);
        item.rename(name);
        return CancelRequestResponse.from(item);
    }

    @Transactional
    public CancelRequestResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CancelRequest item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CancelRequestResponse.from(item);
    }

    @Transactional
    public CancelRequestResponse activate(Long id) {
        CancelRequest item = find(id);
        item.activate();
        log.info("Activated CancelRequest {}", item.getCode());
        return CancelRequestResponse.from(item);
    }

    @Transactional
    public CancelRequestResponse deactivate(Long id) {
        CancelRequest item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CancelRequest {}", item.getCode());
        return CancelRequestResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CancelRequest item = find(id);
        item.archive();
        log.info("Archived CancelRequest {}", item.getCode());
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
        List<CancelRequest> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CancelRequest> items) {
        return items.stream()
                .map(CancelRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CancelRequest item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CancelRequest item, CreateCancelRequestRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderRef(request.orderRef());
        item.setReason(request.reason());
        item.setRequestedBy(request.requestedBy());
        item.setApproved(request.approved());
    }

    private CancelRequest find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
