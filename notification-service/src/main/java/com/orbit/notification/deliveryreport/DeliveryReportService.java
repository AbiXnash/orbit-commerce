package com.orbit.notification.deliveryreport;

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

/** The rules for working with DeliveryReports. */
@Service
public class DeliveryReportService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryReportService.class);

    private final DeliveryReportRepository repository;

    public DeliveryReportService(DeliveryReportRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DeliveryReportResponse create(CreateDeliveryReportRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        DeliveryReport item = new DeliveryReport(request.code(), request.name());
        apply(item, request);
        DeliveryReport saved = repository.save(item);
        log.info("Created DeliveryReport {}", saved.getCode());
        return DeliveryReportResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public DeliveryReportResponse get(Long id) {
        return DeliveryReportResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public DeliveryReportResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(DeliveryReportResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<DeliveryReportResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(DeliveryReportResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DeliveryReportResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(DeliveryReport::getPriority).reversed())
                .limit(limit)
                .map(DeliveryReportResponse::from)
                .toList();
    }

    @Transactional
    public DeliveryReportResponse update(Long id, CreateDeliveryReportRequest request) {
        DeliveryReport item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return DeliveryReportResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        DeliveryReport item = find(id);
        item.archive();
        log.info("Archived DeliveryReport {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(DeliveryReport::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(DeliveryReport item, CreateDeliveryReportRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setMessageCode(request.messageCode());
        item.setProvider(request.provider());
    }

    private DeliveryReport find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
