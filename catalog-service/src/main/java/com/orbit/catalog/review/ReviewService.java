package com.orbit.catalog.review;

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

/** The rules for working with Reviews. */
@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository repository;

    public ReviewService(ReviewRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ReviewResponse create(CreateReviewRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Review item = new Review(request.code(), request.name());
        apply(item, request);
        Review saved = repository.save(item);
        log.info("Created Review {}", saved.getCode());
        return ReviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ReviewResponse get(Long id) {
        return ReviewResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ReviewResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ReviewResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(ReviewResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Review::getPriority).reversed())
                .limit(limit)
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public ReviewResponse update(Long id, CreateReviewRequest request) {
        Review item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return ReviewResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Review item = find(id);
        item.archive();
        log.info("Archived Review {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Review::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Review item, CreateReviewRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setAuthor(request.author());
        item.setHeadline(request.headline());
    }

    private Review find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
