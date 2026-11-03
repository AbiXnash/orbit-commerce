package com.orbit.notification.digest;

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

/** The rules for working with Digests. */
@Service
public class DigestService {

    private static final Logger log = LoggerFactory.getLogger(DigestService.class);

    private final DigestRepository repository;

    public DigestService(DigestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DigestResponse create(CreateDigestRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Digest item = new Digest(request.code(), request.name());
        apply(item, request);
        Digest saved = repository.save(item);
        log.info("Created Digest {}", saved.getCode());
        return DigestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public DigestResponse get(Long id) {
        return DigestResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public DigestResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(DigestResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<DigestResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(DigestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DigestResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Digest::getPriority).reversed())
                .limit(limit)
                .map(DigestResponse::from)
                .toList();
    }

    @Transactional
    public DigestResponse update(Long id, CreateDigestRequest request) {
        Digest item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return DigestResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Digest item = find(id);
        item.archive();
        log.info("Archived Digest {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Digest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Digest item, CreateDigestRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setRecipient(request.recipient());
        item.setFrequency(request.frequency());
    }

    private Digest find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
