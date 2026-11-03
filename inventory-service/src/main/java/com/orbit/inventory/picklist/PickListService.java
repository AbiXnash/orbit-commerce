package com.orbit.inventory.picklist;

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

/** The rules for working with PickList records. */
@Service
public class PickListService {

    private static final Logger log = LoggerFactory.getLogger(PickListService.class);

    private final PickListRepository repository;

    public PickListService(PickListRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PickListResponse create(CreatePickListRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PickList item = new PickList(request.code(), request.name());
        apply(item, request);
        PickList saved = repository.save(item);
        log.info("Created PickList {}", saved.getCode());
        return PickListResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PickListResponse get(Long id) {
        return PickListResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PickListResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PickListResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PickListResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(PickListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PickListResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(PickListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PickListResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PickListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PickListResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(PickListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public PickListResponse update(Long id, CreatePickListRequest request) {
        PickList item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return PickListResponse.from(item);
    }

    @Transactional
    public PickListResponse rename(Long id, String name) {
        PickList item = find(id);
        requireActive(item);
        item.rename(name);
        return PickListResponse.from(item);
    }

    @Transactional
    public PickListResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        PickList item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return PickListResponse.from(item);
    }

    @Transactional
    public PickListResponse activate(Long id) {
        PickList item = find(id);
        item.activate();
        log.info("Activated PickList {}", item.getCode());
        return PickListResponse.from(item);
    }

    @Transactional
    public PickListResponse deactivate(Long id) {
        PickList item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated PickList {}", item.getCode());
        return PickListResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PickList item = find(id);
        item.archive();
        log.info("Archived PickList {}", item.getCode());
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
        List<PickList> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PickList> items) {
        return items.stream()
                .map(PickList::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(PickList item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(PickList item, CreatePickListRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderRef(request.orderRef());
        item.setPickerName(request.pickerName());
        item.setLineCount(request.lineCount());
        item.setPicked(request.picked());
    }

    private PickList find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
