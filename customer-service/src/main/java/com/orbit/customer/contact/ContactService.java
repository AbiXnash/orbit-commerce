package com.orbit.customer.contact;

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

/** The rules for working with Contact records. */
@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ContactResponse create(CreateContactRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Contact item = new Contact(request.code(), request.name());
        apply(item, request);
        Contact saved = repository.save(item);
        log.info("Created Contact {}", saved.getCode());
        return ContactResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ContactResponse get(Long id) {
        return ContactResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ContactResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ContactResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(ContactResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(ContactResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(ContactResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public ContactResponse update(Long id, CreateContactRequest request) {
        Contact item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return ContactResponse.from(item);
    }

    @Transactional
    public ContactResponse rename(Long id, String name) {
        Contact item = find(id);
        requireActive(item);
        item.rename(name);
        return ContactResponse.from(item);
    }

    @Transactional
    public ContactResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Contact item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return ContactResponse.from(item);
    }

    @Transactional
    public ContactResponse activate(Long id) {
        Contact item = find(id);
        item.activate();
        log.info("Activated Contact {}", item.getCode());
        return ContactResponse.from(item);
    }

    @Transactional
    public ContactResponse deactivate(Long id) {
        Contact item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Contact {}", item.getCode());
        return ContactResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Contact item = find(id);
        item.archive();
        log.info("Archived Contact {}", item.getCode());
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
        List<Contact> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Contact> items) {
        return items.stream()
                .map(Contact::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Contact item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Contact item, CreateContactRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setEmail(request.email());
        item.setPhone(request.phone());
        item.setPrimaryContact(request.primaryContact());
    }

    private Contact find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
