package com.orbit.inventory.cyclecount;

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

/** The rules for working with CycleCounts. */
@Service
public class CycleCountService {

    private static final Logger log = LoggerFactory.getLogger(CycleCountService.class);

    private final CycleCountRepository repository;

    public CycleCountService(CycleCountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CycleCountResponse create(CreateCycleCountRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CycleCount item = new CycleCount(request.code(), request.name());
        apply(item, request);
        CycleCount saved = repository.save(item);
        log.info("Created CycleCount {}", saved.getCode());
        return CycleCountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CycleCountResponse get(Long id) {
        return CycleCountResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CycleCountResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CycleCountResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CycleCountResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(CycleCountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CycleCountResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(CycleCount::getPriority).reversed())
                .limit(limit)
                .map(CycleCountResponse::from)
                .toList();
    }

    @Transactional
    public CycleCountResponse update(Long id, CreateCycleCountRequest request) {
        CycleCount item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return CycleCountResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CycleCount item = find(id);
        item.archive();
        log.info("Archived CycleCount {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CycleCount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(CycleCount item, CreateCycleCountRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setZone(request.zone());
        item.setCountedBy(request.countedBy());
    }

    private CycleCount find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
