package com.orbit.notification.message;

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

/** The rules for working with Messages. */
@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MessageResponse create(CreateMessageRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Message item = new Message(request.code(), request.name());
        apply(item, request);
        Message saved = repository.save(item);
        log.info("Created Message {}", saved.getCode());
        return MessageResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MessageResponse get(Long id) {
        return MessageResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public MessageResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(MessageResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(MessageResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Message::getPriority).reversed())
                .limit(limit)
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional
    public MessageResponse update(Long id, CreateMessageRequest request) {
        Message item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return MessageResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Message item = find(id);
        item.archive();
        log.info("Archived Message {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Message::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Message item, CreateMessageRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setRecipient(request.recipient());
        item.setChannel(request.channel());
    }

    private Message find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
