package com.orbit.catalog.supplier;

import com.orbit.common.ConflictException;
import com.orbit.common.ErrorCode;
import com.orbit.common.Limits;
import com.orbit.common.NotFoundException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The rules for working with Suppliers. */
@Service
public class SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository repository;

    public SupplierService(SupplierRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Supplier item = new Supplier(request.code(), request.name());
        apply(item, request);
        Supplier saved = repository.save(item);
        log.info("Created Supplier {}", saved.getCode());
        return SupplierResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long id) {
        return SupplierResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SupplierResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SupplierResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(SupplierResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Supplier::getPriority).reversed())
                .limit(limit)
                .map(SupplierResponse::from)
                .toList();
    }

    @Transactional
    public SupplierResponse update(Long id, CreateSupplierRequest request) {
        Supplier item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return SupplierResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Supplier item = find(id);
        item.archive();
        log.info("Archived Supplier {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Supplier::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Supplier item, CreateSupplierRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setContactEmail(request.contactEmail());
        item.setTaxNumber(request.taxNumber());
    }

    private Supplier find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
