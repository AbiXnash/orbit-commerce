package com.orbit.order.fulfilment;

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

/** The rules for working with Fulfilments. */
@Service
public class FulfilmentService {

    private static final Logger log = LoggerFactory.getLogger(FulfilmentService.class);

    private final FulfilmentRepository repository;

    public FulfilmentService(FulfilmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FulfilmentResponse create(CreateFulfilmentRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Fulfilment item = new Fulfilment(request.code(), request.name());
        apply(item, request);
        Fulfilment saved = repository.save(item);
        log.info("Created Fulfilment {}", saved.getCode());
        return FulfilmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FulfilmentResponse get(Long id) {
        return FulfilmentResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public FulfilmentResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(FulfilmentResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<FulfilmentResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(FulfilmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FulfilmentResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Fulfilment::getPriority).reversed())
                .limit(limit)
                .map(FulfilmentResponse::from)
                .toList();
    }

    @Transactional
    public FulfilmentResponse update(Long id, CreateFulfilmentRequest request) {
        Fulfilment item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return FulfilmentResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Fulfilment item = find(id);
        item.archive();
        log.info("Archived Fulfilment {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Fulfilment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Fulfilment item, CreateFulfilmentRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderCode(request.orderCode());
        item.setWarehouse(request.warehouse());
    }

    private Fulfilment find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
