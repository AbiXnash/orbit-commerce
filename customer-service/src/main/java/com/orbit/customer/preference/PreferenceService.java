package com.orbit.customer.preference;

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

/** The rules for working with Preferences. */
@Service
public class PreferenceService {

    private static final Logger log = LoggerFactory.getLogger(PreferenceService.class);

    private final PreferenceRepository repository;

    public PreferenceService(PreferenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PreferenceResponse create(CreatePreferenceRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Preference item = new Preference(request.code(), request.name());
        apply(item, request);
        Preference saved = repository.save(item);
        log.info("Created Preference {}", saved.getCode());
        return PreferenceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PreferenceResponse get(Long id) {
        return PreferenceResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PreferenceResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(PreferenceResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<PreferenceResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(PreferenceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PreferenceResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Preference::getPriority).reversed())
                .limit(limit)
                .map(PreferenceResponse::from)
                .toList();
    }

    @Transactional
    public PreferenceResponse update(Long id, CreatePreferenceRequest request) {
        Preference item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return PreferenceResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Preference item = find(id);
        item.archive();
        log.info("Archived Preference {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Preference::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Preference item, CreatePreferenceRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setChannel(request.channel());
    }

    private Preference find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
