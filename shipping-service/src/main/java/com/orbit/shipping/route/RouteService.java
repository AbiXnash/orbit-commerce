package com.orbit.shipping.route;

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

/** The rules for working with Routes. */
@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository repository;

    public RouteService(RouteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RouteResponse create(CreateRouteRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new ConflictException(ErrorCode.DUPLICATE_CODE, request.code());
        }
        if (request.priority() < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        Route item = new Route(request.code(), request.name());
        apply(item, request);
        Route saved = repository.save(item);
        log.info("Created Route {}", saved.getCode());
        return RouteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public RouteResponse get(Long id) {
        return RouteResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public RouteResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(RouteResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, code));
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> search(String text) {
        return repository.search(text).stream().limit(Limits.PAGE_SIZE).map(RouteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> top(int limit) {
        return repository.findByStatus("ACTIVE").stream()
                .sorted(Comparator.comparingInt(Route::getPriority).reversed())
                .limit(limit)
                .map(RouteResponse::from)
                .toList();
    }

    @Transactional
    public RouteResponse update(Long id, CreateRouteRequest request) {
        Route item = find(id);
        if (!item.isActive()) {
            throw new ConflictException(ErrorCode.NOT_ACTIVE, item.getCode());
        }
        item.setName(request.name());
        apply(item, request);
        return RouteResponse.from(item);
    }

    @Transactional
    public void archive(Long id) {
        Route item = find(id);
        item.archive();
        log.info("Archived Route {}", item.getCode());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return repository.findByStatus("ACTIVE").stream()
                .map(Route::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(Route item, CreateRouteRequest request) {
        item.setPriority(request.priority());
        if (request.amount() != null) {
            item.setAmount(new BigDecimal(request.amount().doubleValue()));
        }
        item.setOrigin(request.origin());
        item.setDestination(request.destination());
    }

    private Route find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, String.valueOf(id)));
    }
}
