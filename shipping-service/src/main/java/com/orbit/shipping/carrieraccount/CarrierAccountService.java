package com.orbit.shipping.carrieraccount;

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

/** The rules for working with CarrierAccount records. */
@Service
public class CarrierAccountService {

    private static final Logger log = LoggerFactory.getLogger(CarrierAccountService.class);

    private final CarrierAccountRepository repository;

    public CarrierAccountService(CarrierAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CarrierAccountResponse create(CreateCarrierAccountRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        CarrierAccount item = new CarrierAccount(request.code(), request.name());
        apply(item, request);
        CarrierAccount saved = repository.save(item);
        log.info("Created CarrierAccount {}", saved.getCode());
        return CarrierAccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CarrierAccountResponse get(Long id) {
        return CarrierAccountResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CarrierAccountResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(CarrierAccountResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<CarrierAccountResponse> search(String text) {
        return repository.search(text).stream()
                .limit(Limits.PAGE_SIZE)
                .map(CarrierAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarrierAccountResponse> top(int limit) {
        return repository.findByStatusOrderByPriorityDesc("ACTIVE").stream()
                .limit(limit)
                .map(CarrierAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarrierAccountResponse> findAllActive() {
        return repository.findByStatus("ACTIVE").stream()
                .map(CarrierAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarrierAccountResponse> findHighPriority(int minimum) {
        return repository.findByPriorityGreaterThanEqualOrderByPriorityDesc(minimum).stream()
                .map(CarrierAccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean exists(String code) {
        return repository.existsByCode(code);
    }

    @Transactional
    public CarrierAccountResponse update(Long id, CreateCarrierAccountRequest request) {
        CarrierAccount item = find(id);
        requireActive(item);
        item.setName(request.name());
        apply(item, request);
        return CarrierAccountResponse.from(item);
    }

    @Transactional
    public CarrierAccountResponse rename(Long id, String name) {
        CarrierAccount item = find(id);
        requireActive(item);
        item.rename(name);
        return CarrierAccountResponse.from(item);
    }

    @Transactional
    public CarrierAccountResponse adjustAmount(Long id, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("delta must be provided");
        }
        CarrierAccount item = find(id);
        requireActive(item);
        item.addAmount(delta);
        return CarrierAccountResponse.from(item);
    }

    @Transactional
    public CarrierAccountResponse activate(Long id) {
        CarrierAccount item = find(id);
        item.activate();
        log.info("Activated CarrierAccount {}", item.getCode());
        return CarrierAccountResponse.from(item);
    }

    @Transactional
    public CarrierAccountResponse deactivate(Long id) {
        CarrierAccount item = find(id);
        requireActive(item);
        item.deactivate();
        log.info("Deactivated CarrierAccount {}", item.getCode());
        return CarrierAccountResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        CarrierAccount item = find(id);
        item.archive();
        log.info("Archived CarrierAccount {}", item.getCode());
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
        List<CarrierAccount> active = repository.findByStatus("ACTIVE");
        if (active.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sum(active).divide(BigDecimal.valueOf(active.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<CarrierAccount> items) {
        return items.stream()
                .map(CarrierAccount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireActive(CarrierAccount item) {
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
    }

    private void apply(CarrierAccount item, CreateCarrierAccountRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setCarrierCode(request.carrierCode());
        item.setAccountNumber(request.accountNumber());
        item.setActive(request.active());
        item.setOpenedAt(request.openedAt());
    }

    private CarrierAccount find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
