package com.orbit.customer.communicationpreference;

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

/** The rules for working with CommunicationPreference records. */
@Service
public class CommunicationPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(CommunicationPreferenceService.class);

    private final CommunicationPreferenceRepository repository;

    public CommunicationPreferenceService(CommunicationPreferenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CommunicationPreferenceResponse create(CreateCommunicationPreferenceRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CommunicationPreference item = new CommunicationPreference(request.code(), request.name());
        apply(item, request);
        CommunicationPreference saved = repository.save(item);
        log.info("Created CommunicationPreference {}", saved.getCode());
        return CommunicationPreferenceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CommunicationPreferenceResponse get(Long id) {
        return CommunicationPreferenceResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CommunicationPreferenceResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CommunicationPreferenceResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CommunicationPreferenceResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CommunicationPreferenceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommunicationPreferenceResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CommunicationPreferenceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommunicationPreferenceResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CommunicationPreferenceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommunicationPreferenceResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CommunicationPreferenceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CommunicationPreferenceResponse update(Long id, CreateCommunicationPreferenceRequest request) {
        CommunicationPreference item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CommunicationPreferenceResponse.from(item);
    }

    @Transactional
    public CommunicationPreferenceResponse rename(Long id, String name) {
        CommunicationPreference item = find(id);
        requireActive(item);
        item.rename(name);
        return CommunicationPreferenceResponse.from(item);
    }

    @Transactional
    public CommunicationPreferenceResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CommunicationPreference item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CommunicationPreferenceResponse.from(item);
    }

    @Transactional
    public CommunicationPreferenceResponse activate(Long id) {
        CommunicationPreference item = find(id);
        item.activate();
        log.info("Activated CommunicationPreference {}", item.getCode());
        return CommunicationPreferenceResponse.from(item);
    }

    @Transactional
    public CommunicationPreferenceResponse deactivate(Long id) {
        CommunicationPreference item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CommunicationPreference {}", item.getCode());
        return CommunicationPreferenceResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CommunicationPreference item = find(id);
        item.archive();
        log.info("Archived CommunicationPreference {}", item.getCode());
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
        List<CommunicationPreference> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CommunicationPreference> items) {
        return items.stream()
                .map(CommunicationPreference::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CommunicationPreference item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CommunicationPreference item, CreateCommunicationPreferenceRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerRef(request.customerRef());
        item.setChannel(request.channel());
        item.setOptedIn(request.optedIn());
        item.setLastUpdatedAt(request.lastUpdatedAt());
    }

    private CommunicationPreference find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
