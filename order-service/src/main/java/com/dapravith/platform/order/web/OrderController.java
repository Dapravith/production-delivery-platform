package com.dapravith.platform.order.web;
import com.dapravith.platform.order.domain.Order;
import com.dapravith.platform.order.domain.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RestController @RequestMapping("/api/orders")
public class OrderController {
  private final OrderRepository repository;
  public OrderController(OrderRepository repository) { this.repository = repository; }
  @GetMapping @PreAuthorize("hasAuthority('SCOPE_orders.read')")
  Flux<Order> all(@AuthenticationPrincipal Jwt jwt) {
    return repository.findAllByCustomerIdOrderByCreatedAtDesc(jwt.getSubject());
  }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('SCOPE_orders.write')")
  Mono<Order> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateOrder request) {
    return repository.save(new Order(
        UUID.randomUUID(),
        jwt.getSubject(),
        request.amount(),
        request.currency(),
        "CREATED",
        Instant.now()));
  }
  public record CreateOrder(
      @NotNull
      @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
      @DecimalMax(value = "1000000.00", message = "amount must not exceed 1000000.00")
      @Digits(integer = 7, fraction = 2, message = "amount must have at most 2 decimal places")
      BigDecimal amount,
      @NotNull
      @Pattern(regexp = "USD|KHR", message = "currency must be one of: USD, KHR")
      String currency) {}
}
