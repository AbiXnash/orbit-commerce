package com.orbit.inventory.putawaytask;

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

/** The rules for working with PutawayTask records. */
@Service
public class PutawayTaskService {

    private static final Logger log = LoggerFactory.getLogger(PutawayTaskService.class);

    private final PutawayTaskRepository repository;

    public PutawayTaskService(PutawayTaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PutawayTaskResponse create(CreatePutawayTaskRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PutawayTask item = new PutawayTask(request.code(), request.name());
        apply(item, request);
        PutawayTask saved = repository.save(item);
        log.info("Created PutawayTask {}", saved.getCode());
        return PutawayTaskResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PutawayTaskResponse get(Long id) {
        return PutawayTaskResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PutawayTaskResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PutawayTaskResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PutawayTaskResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(PutawayTaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PutawayTaskResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(PutawayTaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PutawayTaskResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PutawayTaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PutawayTaskResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(PutawayTaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public PutawayTaskResponse update(Long id, CreatePutawayTaskRequest request) {
        PutawayTask item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return PutawayTaskResponse.from(item);
    }

    @Transactional
    public PutawayTaskResponse rename(Long id, String name) {
        PutawayTask item = find(id);
        requireActive(item);
        item.rename(name);
        return PutawayTaskResponse.from(item);
    }

    @Transactional
    public PutawayTaskResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        PutawayTask item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return PutawayTaskResponse.from(item);
    }

    @Transactional
    public PutawayTaskResponse activate(Long id) {
        PutawayTask item = find(id);
        item.activate();
        log.info("Activated PutawayTask {}", item.getCode());
        return PutawayTaskResponse.from(item);
    }

    @Transactional
    public PutawayTaskResponse deactivate(Long id) {
        PutawayTask item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated PutawayTask {}", item.getCode());
        return PutawayTaskResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PutawayTask item = find(id);
        item.archive();
        log.info("Archived PutawayTask {}", item.getCode());
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
        List<PutawayTask> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PutawayTask> items) {
        return items.stream()
                .map(PutawayTask::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(PutawayTask item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(PutawayTask item, CreatePutawayTaskRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setInboundRef(request.inboundRef());
        item.setBinCode(request.binCode());
        item.setQuantity(request.quantity());
        item.setCompleted(request.completed());
    }

    private PutawayTask find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
