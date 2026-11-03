package com.orbit.customer.surveyresponse;

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

/** The rules for working with SurveyResponse records. */
@Service
public class SurveyResponseService {

    private static final Logger log = LoggerFactory.getLogger(SurveyResponseService.class);

    private final SurveyResponseRepository repository;

    public SurveyResponseService(SurveyResponseRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SurveyResponseResponse create(CreateSurveyResponseRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        SurveyResponse item = new SurveyResponse(request.code(), request.name());
        apply(item, request);
        SurveyResponse saved = repository.save(item);
        log.info("Created SurveyResponse {}", saved.getCode());
        return SurveyResponseResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SurveyResponseResponse get(Long id) {
        return SurveyResponseResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SurveyResponseResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SurveyResponseResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SurveyResponseResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(SurveyResponseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SurveyResponseResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(SurveyResponseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SurveyResponseResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(SurveyResponseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SurveyResponseResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(SurveyResponseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public SurveyResponseResponse update(Long id, CreateSurveyResponseRequest request) {
        SurveyResponse item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return SurveyResponseResponse.from(item);
    }

    @Transactional
    public SurveyResponseResponse rename(Long id, String name) {
        SurveyResponse item = find(id);
        requireActive(item);
        item.rename(name);
        return SurveyResponseResponse.from(item);
    }

    @Transactional
    public SurveyResponseResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        SurveyResponse item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return SurveyResponseResponse.from(item);
    }

    @Transactional
    public SurveyResponseResponse activate(Long id) {
        SurveyResponse item = find(id);
        item.activate();
        log.info("Activated SurveyResponse {}", item.getCode());
        return SurveyResponseResponse.from(item);
    }

    @Transactional
    public SurveyResponseResponse deactivate(Long id) {
        SurveyResponse item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated SurveyResponse {}", item.getCode());
        return SurveyResponseResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        SurveyResponse item = find(id);
        item.archive();
        log.info("Archived SurveyResponse {}", item.getCode());
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
        List<SurveyResponse> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<SurveyResponse> items) {
        return items.stream()
                .map(SurveyResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(SurveyResponse item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(SurveyResponse item, CreateSurveyResponseRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSurveyRef(request.surveyRef());
        item.setCustomerRef(request.customerRef());
        item.setComment(request.comment());
        item.setSubmittedAt(request.submittedAt());
    }

    private SurveyResponse find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
