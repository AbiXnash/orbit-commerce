package com.orbit.notification.channel;

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

/** The rules for working with Channel records. */
@Service
public class ChannelService {

    private static final Logger log = LoggerFactory.getLogger(ChannelService.class);

    private final ChannelRepository repository;

    public ChannelService(ChannelRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ChannelResponse create(CreateChannelRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Channel item = new Channel(request.code(), request.name());
        apply(item, request);
        Channel saved = repository.save(item);
        log.info("Created Channel {}", saved.getCode());
        return ChannelResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ChannelResponse get(Long id) {
        return ChannelResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ChannelResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(ChannelResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<ChannelResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(ChannelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChannelResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(ChannelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChannelResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(ChannelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChannelResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(ChannelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public ChannelResponse update(Long id, CreateChannelRequest request) {
        Channel item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return ChannelResponse.from(item);
    }

    @Transactional
    public ChannelResponse rename(Long id, String name) {
        Channel item = find(id);
        requireActive(item);
        item.rename(name);
        return ChannelResponse.from(item);
    }

    @Transactional
    public ChannelResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        Channel item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return ChannelResponse.from(item);
    }

    @Transactional
    public ChannelResponse activate(Long id) {
        Channel item = find(id);
        item.activate();
        log.info("Activated Channel {}", item.getCode());
        return ChannelResponse.from(item);
    }

    @Transactional
    public ChannelResponse deactivate(Long id) {
        Channel item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated Channel {}", item.getCode());
        return ChannelResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Channel item = find(id);
        item.archive();
        log.info("Archived Channel {}", item.getCode());
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
        List<Channel> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<Channel> items) {
        return items.stream()
                .map(Channel::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(Channel item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(Channel item, CreateChannelRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setChannelType(request.channelType());
        item.setProvider(request.provider());
        item.setEnabled(request.enabled());
        item.setRateLimit(request.rateLimit());
    }

    private Channel find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
