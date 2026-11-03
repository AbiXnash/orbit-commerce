package com.orbit.customer.customergroup;

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

/** The rules for working with CustomerGroup records. */
@Service
public class CustomerGroupService {

    private static final Logger log = LoggerFactory.getLogger(CustomerGroupService.class);

    private final CustomerGroupRepository repository;

    public CustomerGroupService(CustomerGroupRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerGroupResponse create(CreateCustomerGroupRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CustomerGroup item = new CustomerGroup(request.code(), request.name());
        apply(item, request);
        CustomerGroup saved = repository.save(item);
        log.info("Created CustomerGroup {}", saved.getCode());
        return CustomerGroupResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CustomerGroupResponse get(Long id) {
        return CustomerGroupResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CustomerGroupResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CustomerGroupResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CustomerGroupResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CustomerGroupResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerGroupResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CustomerGroupResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerGroupResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CustomerGroupResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerGroupResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CustomerGroupResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CustomerGroupResponse update(Long id, CreateCustomerGroupRequest request) {
        CustomerGroup item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CustomerGroupResponse.from(item);
    }

    @Transactional
    public CustomerGroupResponse rename(Long id, String name) {
        CustomerGroup item = find(id);
        requireActive(item);
        item.rename(name);
        return CustomerGroupResponse.from(item);
    }

    @Transactional
    public CustomerGroupResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CustomerGroup item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CustomerGroupResponse.from(item);
    }

    @Transactional
    public CustomerGroupResponse activate(Long id) {
        CustomerGroup item = find(id);
        item.activate();
        log.info("Activated CustomerGroup {}", item.getCode());
        return CustomerGroupResponse.from(item);
    }

    @Transactional
    public CustomerGroupResponse deactivate(Long id) {
        CustomerGroup item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CustomerGroup {}", item.getCode());
        return CustomerGroupResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CustomerGroup item = find(id);
        item.archive();
        log.info("Archived CustomerGroup {}", item.getCode());
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
        List<CustomerGroup> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CustomerGroup> items) {
        return items.stream()
                .map(CustomerGroup::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CustomerGroup item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CustomerGroup item, CreateCustomerGroupRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSlug(request.slug());
        item.setDescription(request.description());
        item.setMemberCount(request.memberCount());
        item.setFeatured(request.featured());
    }

    private CustomerGroup find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
