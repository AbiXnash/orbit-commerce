package com.orbit.catalog.catalogimport;

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

/** The rules for working with CatalogImport records. */
@Service
public class CatalogImportService {

    private static final Logger log = LoggerFactory.getLogger(CatalogImportService.class);

    private final CatalogImportRepository repository;

    public CatalogImportService(CatalogImportRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CatalogImportResponse create(CreateCatalogImportRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CatalogImport item = new CatalogImport(request.code(), request.name());
        apply(item, request);
        CatalogImport saved = repository.save(item);
        log.info("Created CatalogImport {}", saved.getCode());
        return CatalogImportResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CatalogImportResponse get(Long id) {
        return CatalogImportResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CatalogImportResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CatalogImportResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CatalogImportResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CatalogImportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogImportResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CatalogImportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogImportResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CatalogImportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogImportResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CatalogImportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CatalogImportResponse update(Long id, CreateCatalogImportRequest request) {
        CatalogImport item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CatalogImportResponse.from(item);
    }

    @Transactional
    public CatalogImportResponse rename(Long id, String name) {
        CatalogImport item = find(id);
        requireActive(item);
        item.rename(name);
        return CatalogImportResponse.from(item);
    }

    @Transactional
    public CatalogImportResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CatalogImport item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CatalogImportResponse.from(item);
    }

    @Transactional
    public CatalogImportResponse activate(Long id) {
        CatalogImport item = find(id);
        item.activate();
        log.info("Activated CatalogImport {}", item.getCode());
        return CatalogImportResponse.from(item);
    }

    @Transactional
    public CatalogImportResponse deactivate(Long id) {
        CatalogImport item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CatalogImport {}", item.getCode());
        return CatalogImportResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CatalogImport item = find(id);
        item.archive();
        log.info("Archived CatalogImport {}", item.getCode());
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
        List<CatalogImport> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CatalogImport> items) {
        return items.stream()
                .map(CatalogImport::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CatalogImport item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CatalogImport item, CreateCatalogImportRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSource(request.source());
        item.setFileName(request.fileName());
        item.setRowCount(request.rowCount());
        item.setChecksum(request.checksum());
    }

    private CatalogImport find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
