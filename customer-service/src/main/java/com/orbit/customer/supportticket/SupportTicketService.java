package com.orbit.customer.supportticket;

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

/** The rules for working with SupportTickets. */
@Service
public class SupportTicketService {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketService.class);

    private final SupportTicketRepository repository;

    public SupportTicketService(SupportTicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SupportTicketResponse create(CreateSupportTicketRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        SupportTicket item = new SupportTicket(request.code(), request.name());
        apply(item, request);
        SupportTicket saved = repository.save(item);
        log.info("Created SupportTicket {}", saved.getCode());
        return SupportTicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SupportTicketResponse get(Long id) {
        return SupportTicketResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public SupportTicketResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(SupportTicketResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(SupportTicketResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(SupportTicket::getPriority).reversed())
                .limit(limit)
                .map(SupportTicketResponse::from)
                .toList();
    }

    @Transactional
    public SupportTicketResponse update(Long id, CreateSupportTicketRequest request) {
        SupportTicket item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return SupportTicketResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        SupportTicket item = find(id);
        item.archive();
        log.info("Archived SupportTicket {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(SupportTicket::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(SupportTicket item, CreateSupportTicketRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCustomerCode(request.customerCode());
        item.setSubject(request.subject());
    }

    private SupportTicket find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
