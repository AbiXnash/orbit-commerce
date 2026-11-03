package com.orbit.order.returnrequest;

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

/** The rules for working with ReturnRequests. */
@Service
public class ReturnRequestService {

    private static final Logger log = LoggerFactory.getLogger(ReturnRequestService.class);

    private final ReturnRequestRepository repository;

    public ReturnRequestService(ReturnRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ReturnRequestResponse create(CreateReturnRequestRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        ReturnRequest item = new ReturnRequest(request.code(), request.name());
        apply(item, request);
        ReturnRequest saved = repository.save(item);
        log.info("Created ReturnRequest {}", saved.getCode());
        return ReturnRequestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ReturnRequestResponse get(Long id) {
        return ReturnRequestResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ReturnRequestResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ReturnRequestResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(ReturnRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReturnRequestResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(ReturnRequest::getPriority).reversed())
                .limit(limit)
                .map(ReturnRequestResponse::from)
                .toList();
    }

    @Transactional
    public ReturnRequestResponse update(Long id, CreateReturnRequestRequest request) {
        ReturnRequest item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return ReturnRequestResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        ReturnRequest item = find(id);
        item.archive();
        log.info("Archived ReturnRequest {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ReturnRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(ReturnRequest item, CreateReturnRequestRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderCode(request.orderCode());
        item.setReason(request.reason());
    }

    private ReturnRequest find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
