package com.orbit.catalog.collection;

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

/** The rules for working with Collections. */
@Service
public class CollectionService {

    private static final Logger log = LoggerFactory.getLogger(CollectionService.class);

    private final CollectionRepository repository;

    public CollectionService(CollectionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CollectionResponse create(CreateCollectionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Collection item = new Collection(request.code(), request.name());
        apply(item, request);
        Collection saved = repository.save(item);
        log.info("Created Collection {}", saved.getCode());
        return CollectionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CollectionResponse get(Long id) {
        return CollectionResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CollectionResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CollectionResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(CollectionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Collection::getPriority).reversed())
                .limit(limit)
                .map(CollectionResponse::from)
                .toList();
    }

    @Transactional
    public CollectionResponse update(Long id, CreateCollectionRequest request) {
        Collection item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return CollectionResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Collection item = find(id);
        item.archive();
        log.info("Archived Collection {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Collection::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Collection item, CreateCollectionRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setSeason(request.season());
        item.setTheme(request.theme());
    }

    private Collection find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
