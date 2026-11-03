package com.orbit.shipping.customsdocument;

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

/** The rules for working with CustomsDocument records. */
@Service
public class CustomsDocumentService {

    private static final Logger log = LoggerFactory.getLogger(CustomsDocumentService.class);

    private final CustomsDocumentRepository repository;

    public CustomsDocumentService(CustomsDocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomsDocumentResponse create(CreateCustomsDocumentRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CustomsDocument item = new CustomsDocument(request.code(), request.name());
        apply(item, request);
        CustomsDocument saved = repository.save(item);
        log.info("Created CustomsDocument {}", saved.getCode());
        return CustomsDocumentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CustomsDocumentResponse get(Long id) {
        return CustomsDocumentResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CustomsDocumentResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CustomsDocumentResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CustomsDocumentResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CustomsDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomsDocumentResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CustomsDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomsDocumentResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CustomsDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomsDocumentResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CustomsDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CustomsDocumentResponse update(Long id, CreateCustomsDocumentRequest request) {
        CustomsDocument item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CustomsDocumentResponse.from(item);
    }

    @Transactional
    public CustomsDocumentResponse rename(Long id, String name) {
        CustomsDocument item = find(id);
        requireActive(item);
        item.rename(name);
        return CustomsDocumentResponse.from(item);
    }

    @Transactional
    public CustomsDocumentResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CustomsDocument item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CustomsDocumentResponse.from(item);
    }

    @Transactional
    public CustomsDocumentResponse activate(Long id) {
        CustomsDocument item = find(id);
        item.activate();
        log.info("Activated CustomsDocument {}", item.getCode());
        return CustomsDocumentResponse.from(item);
    }

    @Transactional
    public CustomsDocumentResponse deactivate(Long id) {
        CustomsDocument item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CustomsDocument {}", item.getCode());
        return CustomsDocumentResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CustomsDocument item = find(id);
        item.archive();
        log.info("Archived CustomsDocument {}", item.getCode());
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
        List<CustomsDocument> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CustomsDocument> items) {
        return items.stream()
                .map(CustomsDocument::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CustomsDocument item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CustomsDocument item, CreateCustomsDocumentRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setShipmentRef(request.shipmentRef());
        item.setDocumentType(request.documentType());
        item.setCleared(request.cleared());
        if (request.declaredValue() != null) {
            item.setDeclaredValue(request.declaredValue());
        }
    }

    private CustomsDocument find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
