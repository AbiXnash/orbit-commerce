package com.orbit.inventory.stockitem;

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

/** The rules for working with StockItems. */
@Service
public class StockItemService {

    private static final Logger log = LoggerFactory.getLogger(StockItemService.class);

    private final StockItemRepository repository;

    public StockItemService(StockItemRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StockItemResponse create(CreateStockItemRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        StockItem item = new StockItem(request.code(), request.name());
        apply(item, request);
        StockItem saved = repository.save(item);
        log.info("Created StockItem {}", saved.getCode());
        return StockItemResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public StockItemResponse get(Long id) {
        return StockItemResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public StockItemResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(StockItemResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(StockItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(StockItem::getPriority).reversed())
                .limit(limit)
                .map(StockItemResponse::from)
                .toList();
    }

    @Transactional
    public StockItemResponse update(Long id, CreateStockItemRequest request) {
        StockItem item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return StockItemResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        StockItem item = find(id);
        item.archive();
        log.info("Archived StockItem {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(StockItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(StockItem item, CreateStockItemRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSku(request.sku());
        item.setBinLocation(request.binLocation());
    }

    private StockItem find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
