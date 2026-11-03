package com.orbit.shipping.manifest;

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

/** The rules for working with Manifests. */
@Service
public class ManifestService {

    private static final Logger log = LoggerFactory.getLogger(ManifestService.class);

    private final ManifestRepository repository;

    public ManifestService(ManifestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ManifestResponse create(CreateManifestRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Manifest item = new Manifest(request.code(), request.name());
        apply(item, request);
        Manifest saved = repository.save(item);
        log.info("Created Manifest {}", saved.getCode());
        return ManifestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ManifestResponse get(Long id) {
        return ManifestResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ManifestResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ManifestResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ManifestResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(ManifestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ManifestResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Manifest::getPriority).reversed())
                .limit(limit)
                .map(ManifestResponse::from)
                .toList();
    }

    @Transactional
    public ManifestResponse update(Long id, CreateManifestRequest request) {
        Manifest item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return ManifestResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Manifest item = find(id);
        item.archive();
        log.info("Archived Manifest {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Manifest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Manifest item, CreateManifestRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCarrierCode(request.carrierCode());
        item.setDepot(request.depot());
    }

    private Manifest find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
