package com.orbit.catalog.pricelist;

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

/** The rules for working with PriceLists. */
@Service
public class PriceListService {

    private static final Logger log = LoggerFactory.getLogger(PriceListService.class);

    private final PriceListRepository repository;

    public PriceListService(PriceListRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PriceListResponse create(CreatePriceListRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        PriceList item = new PriceList(request.code(), request.name());
        apply(item, request);
        PriceList saved = repository.save(item);
        log.info("Created PriceList {}", saved.getCode());
        return PriceListResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PriceListResponse get(Long id) {
        return PriceListResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PriceListResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PriceListResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PriceListResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(PriceListResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PriceListResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(PriceList::getPriority).reversed())
                .limit(limit)
                .map(PriceListResponse::from)
                .toList();
    }

    @Transactional
    public PriceListResponse update(Long id, CreatePriceListRequest request) {
        PriceList item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return PriceListResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        PriceList item = find(id);
        item.archive();
        log.info("Archived PriceList {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(PriceList::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(PriceList item, CreatePriceListRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCurrency(request.currency());
        item.setRegion(request.region());
    }

    private PriceList find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
