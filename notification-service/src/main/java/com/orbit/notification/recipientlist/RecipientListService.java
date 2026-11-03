package com.orbit.notification.recipientlist;

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

/** The rules for working with RecipientList records. */
@Service
public class RecipientListService {

    private static final Logger log = LoggerFactory.getLogger(RecipientListService.class);

    private final RecipientListRepository repository;

    public RecipientListService(RecipientListRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RecipientListResponse create(CreateRecipientListRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        RecipientList item = new RecipientList(request.code(), request.name());
        apply(item, request);
        RecipientList saved = repository.save(item);
        log.info("Created RecipientList {}", saved.getCode());
        return RecipientListResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public RecipientListResponse get(Long id) {
        return RecipientListResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public RecipientListResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(RecipientListResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<RecipientListResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(RecipientListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipientListResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(RecipientListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipientListResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(RecipientListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipientListResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(RecipientListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public RecipientListResponse update(Long id, CreateRecipientListRequest request) {
        RecipientList item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return RecipientListResponse.from(item);
    }

    @Transactional
    public RecipientListResponse rename(Long id, String name) {
        RecipientList item = find(id);
        requireActive(item);
        item.rename(name);
        return RecipientListResponse.from(item);
    }

    @Transactional
    public RecipientListResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        RecipientList item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return RecipientListResponse.from(item);
    }

    @Transactional
    public RecipientListResponse activate(Long id) {
        RecipientList item = find(id);
        item.activate();
        log.info("Activated RecipientList {}", item.getCode());
        return RecipientListResponse.from(item);
    }

    @Transactional
    public RecipientListResponse deactivate(Long id) {
        RecipientList item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated RecipientList {}", item.getCode());
        return RecipientListResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        RecipientList item = find(id);
        item.archive();
        log.info("Archived RecipientList {}", item.getCode());
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
        List<RecipientList> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<RecipientList> items) {
        return items.stream()
                .map(RecipientList::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(RecipientList item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(RecipientList item, CreateRecipientListRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setListName(request.listName());
        item.setMemberCount(request.memberCount());
        item.setDynamic(request.dynamic());
        item.setRefreshedAt(request.refreshedAt());
    }

    private RecipientList find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
