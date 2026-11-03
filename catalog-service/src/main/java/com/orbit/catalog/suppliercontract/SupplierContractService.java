package com.orbit.catalog.suppliercontract;

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

/** The rules for working with SupplierContract records. */
@Service
public class SupplierContractService {

    private static final Logger log = LoggerFactory.getLogger(SupplierContractService.class);

    private final SupplierContractRepository repository;

    public SupplierContractService(SupplierContractRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SupplierContractResponse create(CreateSupplierContractRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        SupplierContract item = new SupplierContract(request.code(), request.name());
        apply(item, request);
        SupplierContract saved = repository.save(item);
        log.info("Created SupplierContract {}", saved.getCode());
        return SupplierContractResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SupplierContractResponse get(Long id) {
        return SupplierContractResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SupplierContractResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SupplierContractResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SupplierContractResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(SupplierContractResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierContractResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(SupplierContractResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierContractResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(SupplierContractResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplierContractResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(SupplierContractResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public SupplierContractResponse update(Long id, CreateSupplierContractRequest request) {
        SupplierContract item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return SupplierContractResponse.from(item);
    }

    @Transactional
    public SupplierContractResponse rename(Long id, String name) {
        SupplierContract item = find(id);
        requireActive(item);
        item.rename(name);
        return SupplierContractResponse.from(item);
    }

    @Transactional
    public SupplierContractResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        SupplierContract item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return SupplierContractResponse.from(item);
    }

    @Transactional
    public SupplierContractResponse activate(Long id) {
        SupplierContract item = find(id);
        item.activate();
        log.info("Activated SupplierContract {}", item.getCode());
        return SupplierContractResponse.from(item);
    }

    @Transactional
    public SupplierContractResponse deactivate(Long id) {
        SupplierContract item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated SupplierContract {}", item.getCode());
        return SupplierContractResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        SupplierContract item = find(id);
        item.archive();
        log.info("Archived SupplierContract {}", item.getCode());
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
        List<SupplierContract> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<SupplierContract> items) {
        return items.stream()
                .map(SupplierContract::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(SupplierContract item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(SupplierContract item, CreateSupplierContractRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setContractRef(request.contractRef());
        item.setRenewalNoticeDays(request.renewalNoticeDays());
        item.setSigned(request.signed());
        item.setSignedAt(request.signedAt());
    }

    private SupplierContract find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
