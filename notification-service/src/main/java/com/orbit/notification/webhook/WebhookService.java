package com.orbit.notification.webhook;

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

/** The rules for working with Webhooks. */
@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookRepository repository;

    public WebhookService(WebhookRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WebhookResponse create(CreateWebhookRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Webhook item = new Webhook(request.code(), request.name());
        apply(item, request);
        Webhook saved = repository.save(item);
        log.info("Created Webhook {}", saved.getCode());
        return WebhookResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public WebhookResponse get(Long id) {
        return WebhookResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public WebhookResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(WebhookResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<WebhookResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(WebhookResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<WebhookResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Webhook::getPriority).reversed())
                .limit(limit)
                .map(WebhookResponse::from)
                .toList();
    }

    @Transactional
    public WebhookResponse update(Long id, CreateWebhookRequest request) {
        Webhook item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return WebhookResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Webhook item = find(id);
        item.archive();
        log.info("Archived Webhook {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Webhook::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Webhook item, CreateWebhookRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setUrl(request.url());
        item.setEventType(request.eventType());
    }

    private Webhook find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
