package com.orbit.customer.identitydocument;

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

/** The rules for working with IdentityDocument records. */
@Service
public class IdentityDocumentService {

    private static final Logger log = LoggerFactory.getLogger(IdentityDocumentService.class);

    private final IdentityDocumentRepository repository;

    public IdentityDocumentService(IdentityDocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IdentityDocumentResponse create(CreateIdentityDocumentRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        IdentityDocument item = new IdentityDocument(request.code(), request.name());
        apply(item, request);
        IdentityDocument saved = repository.save(item);
        log.info("Created IdentityDocument {}", saved.getCode());
        return IdentityDocumentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public IdentityDocumentResponse get(Long id) {
        return IdentityDocumentResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public IdentityDocumentResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(IdentityDocumentResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<IdentityDocumentResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(IdentityDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IdentityDocumentResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(IdentityDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IdentityDocumentResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(IdentityDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IdentityDocumentResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(IdentityDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public IdentityDocumentResponse update(Long id, CreateIdentityDocumentRequest request) {
        IdentityDocument item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return IdentityDocumentResponse.from(item);
    }

    @Transactional
    public IdentityDocumentResponse rename(Long id, String name) {
        IdentityDocument item = find(id);
        requireActive(item);
        item.rename(name);
        return IdentityDocumentResponse.from(item);
    }

    @Transactional
    public IdentityDocumentResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        IdentityDocument item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return IdentityDocumentResponse.from(item);
    }

    @Transactional
    public IdentityDocumentResponse activate(Long id) {
        IdentityDocument item = find(id);
        item.activate();
        log.info("Activated IdentityDocument {}", item.getCode());
        return IdentityDocumentResponse.from(item);
    }

    @Transactional
    public IdentityDocumentResponse deactivate(Long id) {
        IdentityDocument item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated IdentityDocument {}", item.getCode());
        return IdentityDocumentResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        IdentityDocument item = find(id);
        item.archive();
        log.info("Archived IdentityDocument {}", item.getCode());
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
        List<IdentityDocument> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<IdentityDocument> items) {
        return items.stream()
                .map(IdentityDocument::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(IdentityDocument item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(IdentityDocument item, CreateIdentityDocumentRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setDocumentType(request.documentType());
        item.setDocumentNumber(request.documentNumber());
        item.setVerified(request.verified());
    }

    private IdentityDocument find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
