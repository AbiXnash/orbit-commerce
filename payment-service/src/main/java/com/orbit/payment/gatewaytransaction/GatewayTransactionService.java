package com.orbit.payment.gatewaytransaction;

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

/** The rules for working with GatewayTransaction records. */
@Service
public class GatewayTransactionService {

    private static final Logger log = LoggerFactory.getLogger(GatewayTransactionService.class);

    private final GatewayTransactionRepository repository;

    public GatewayTransactionService(GatewayTransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public GatewayTransactionResponse create(CreateGatewayTransactionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        GatewayTransaction item = new GatewayTransaction(request.code(), request.name());
        apply(item, request);
        GatewayTransaction saved = repository.save(item);
        log.info("Created GatewayTransaction {}", saved.getCode());
        return GatewayTransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public GatewayTransactionResponse get(Long id) {
        return GatewayTransactionResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public GatewayTransactionResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(GatewayTransactionResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<GatewayTransactionResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(GatewayTransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GatewayTransactionResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(GatewayTransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GatewayTransactionResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(GatewayTransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GatewayTransactionResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(GatewayTransactionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public GatewayTransactionResponse update(Long id, CreateGatewayTransactionRequest request) {
        GatewayTransaction item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return GatewayTransactionResponse.from(item);
    }

    @Transactional
    public GatewayTransactionResponse rename(Long id, String name) {
        GatewayTransaction item = find(id);
        requireActive(item);
        item.rename(name);
        return GatewayTransactionResponse.from(item);
    }

    @Transactional
    public GatewayTransactionResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        GatewayTransaction item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return GatewayTransactionResponse.from(item);
    }

    @Transactional
    public GatewayTransactionResponse activate(Long id) {
        GatewayTransaction item = find(id);
        item.activate();
        log.info("Activated GatewayTransaction {}", item.getCode());
        return GatewayTransactionResponse.from(item);
    }

    @Transactional
    public GatewayTransactionResponse deactivate(Long id) {
        GatewayTransaction item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated GatewayTransaction {}", item.getCode());
        return GatewayTransactionResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        GatewayTransaction item = find(id);
        item.archive();
        log.info("Archived GatewayTransaction {}", item.getCode());
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
        List<GatewayTransaction> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<GatewayTransaction> items) {
        return items.stream()
                .map(GatewayTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(GatewayTransaction item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(GatewayTransaction item, CreateGatewayTransactionRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setGatewayRef(request.gatewayRef());
        item.setRequestId(request.requestId());
        item.setResponseCode(request.responseCode());
        item.setLatencyMs(request.latencyMs());
    }

    private GatewayTransaction find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
