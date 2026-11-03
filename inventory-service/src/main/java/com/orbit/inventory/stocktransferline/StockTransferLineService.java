package com.orbit.inventory.stocktransferline;

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

/** The rules for working with StockTransferLine records. */
@Service
public class StockTransferLineService {

    private static final Logger log = LoggerFactory.getLogger(StockTransferLineService.class);

    private final StockTransferLineRepository repository;

    public StockTransferLineService(StockTransferLineRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StockTransferLineResponse create(CreateStockTransferLineRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        StockTransferLine item = new StockTransferLine(request.code(), request.name());
        apply(item, request);
        StockTransferLine saved = repository.save(item);
        log.info("Created StockTransferLine {}", saved.getCode());
        return StockTransferLineResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public StockTransferLineResponse get(Long id) {
        return StockTransferLineResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public StockTransferLineResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(StockTransferLineResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<StockTransferLineResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(StockTransferLineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockTransferLineResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(StockTransferLineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockTransferLineResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(StockTransferLineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockTransferLineResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(StockTransferLineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public StockTransferLineResponse update(Long id, CreateStockTransferLineRequest request) {
        StockTransferLine item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return StockTransferLineResponse.from(item);
    }

    @Transactional
    public StockTransferLineResponse rename(Long id, String name) {
        StockTransferLine item = find(id);
        requireActive(item);
        item.rename(name);
        return StockTransferLineResponse.from(item);
    }

    @Transactional
    public StockTransferLineResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        StockTransferLine item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return StockTransferLineResponse.from(item);
    }

    @Transactional
    public StockTransferLineResponse activate(Long id) {
        StockTransferLine item = find(id);
        item.activate();
        log.info("Activated StockTransferLine {}", item.getCode());
        return StockTransferLineResponse.from(item);
    }

    @Transactional
    public StockTransferLineResponse deactivate(Long id) {
        StockTransferLine item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated StockTransferLine {}", item.getCode());
        return StockTransferLineResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        StockTransferLine item = find(id);
        item.archive();
        log.info("Archived StockTransferLine {}", item.getCode());
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
        List<StockTransferLine> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<StockTransferLine> items) {
        return items.stream()
                .map(StockTransferLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(StockTransferLine item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(StockTransferLine item, CreateStockTransferLineRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setTransferRef(request.transferRef());
        item.setProductCode(request.productCode());
        item.setQuantity(request.quantity());
        item.setReceivedQuantity(request.receivedQuantity());
    }

    private StockTransferLine find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
