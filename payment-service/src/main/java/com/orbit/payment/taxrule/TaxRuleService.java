package com.orbit.payment.taxrule;

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

/** The rules for working with TaxRules. */
@Service
public class TaxRuleService {

    private static final Logger log = LoggerFactory.getLogger(TaxRuleService.class);

    private final TaxRuleRepository repository;

    public TaxRuleService(TaxRuleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TaxRuleResponse create(CreateTaxRuleRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        TaxRule item = new TaxRule(request.code(), request.name());
        apply(item, request);
        TaxRule saved = repository.save(item);
        log.info("Created TaxRule {}", saved.getCode());
        return TaxRuleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TaxRuleResponse get(Long id) {
        return TaxRuleResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public TaxRuleResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(TaxRuleResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<TaxRuleResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(TaxRuleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TaxRuleResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(TaxRule::getPriority).reversed())
                .limit(limit)
                .map(TaxRuleResponse::from)
                .toList();
    }

    @Transactional
    public TaxRuleResponse update(Long id, CreateTaxRuleRequest request) {
        TaxRule item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return TaxRuleResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        TaxRule item = find(id);
        item.archive();
        log.info("Archived TaxRule {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(TaxRule::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(TaxRule item, CreateTaxRuleRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(request.amount());
        }
        item.setRegion(request.region());
        item.setCategory(request.category());
    }

    private TaxRule find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
