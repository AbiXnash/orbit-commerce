package com.orbit.inventory.transfer;

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

/** The rules for working with Transfers. */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepository repository;

    public TransferService(TransferRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TransferResponse create(CreateTransferRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Transfer item = new Transfer(request.code(), request.name());
        apply(item, request);
        Transfer saved = repository.save(item);
        log.info("Created Transfer {}", saved.getCode());
        return TransferResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TransferResponse get(Long id) {
        return TransferResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public TransferResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(TransferResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(TransferResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Transfer::getPriority).reversed())
                .limit(limit)
                .map(TransferResponse::from)
                .toList();
    }

    @Transactional
    public TransferResponse update(Long id, CreateTransferRequest request) {
        Transfer item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return TransferResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Transfer item = find(id);
        item.archive();
        log.info("Archived Transfer {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Transfer item, CreateTransferRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setFromSite(request.fromSite());
        item.setToSite(request.toSite());
    }

    private Transfer find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
