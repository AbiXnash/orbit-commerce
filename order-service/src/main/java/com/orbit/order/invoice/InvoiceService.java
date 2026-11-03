package com.orbit.order.invoice;

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

/** The rules for working with Invoices. */
@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Invoice item = new Invoice(request.code(), request.name());
        apply(item, request);
        Invoice saved = repository.save(item);
        log.info("Created Invoice {}", saved.getCode());
        return InvoiceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(Long id) {
        return InvoiceResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(InvoiceResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(InvoiceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Invoice::getPriority).reversed())
                .limit(limit)
                .map(InvoiceResponse::from)
                .toList();
    }

    @Transactional
    public InvoiceResponse update(Long id, CreateInvoiceRequest request) {
        Invoice item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return InvoiceResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Invoice item = find(id);
        item.archive();
        log.info("Archived Invoice {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Invoice item, CreateInvoiceRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setOrderCode(request.orderCode());
        item.setTaxNumber(request.taxNumber());
    }

    private Invoice find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
