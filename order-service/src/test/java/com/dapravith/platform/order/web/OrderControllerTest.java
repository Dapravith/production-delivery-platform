package com.dapravith.platform.order.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dapravith.platform.order.domain.Order;
import com.dapravith.platform.order.domain.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OrderControllerTest {
  private final OrderRepository repository = mock(OrderRepository.class);
  private final OrderController controller = new OrderController(repository);
  private final Jwt jwt = Jwt.withTokenValue("test-token")
      .header("alg", "none").subject("customer-123").build();

  @Test
  void listsOnlyAuthenticatedCustomersOrders() {
    var order = new Order(
        UUID.randomUUID(),
        "customer-123",
        new BigDecimal("25.00"),
        "USD",
        "CREATED",
        Instant.now(),
        0L);
    when(repository.findAllByCustomerIdOrderByCreatedAtDesc("customer-123")).thenReturn(Flux.just(order));

    StepVerifier.create(controller.all(jwt)).expectNext(order).verifyComplete();
    verify(repository).findAllByCustomerIdOrderByCreatedAtDesc("customer-123");
  }

  @Test
  void derivesCustomerIdentityFromJwtWhenCreatingOrder() {
    when(repository.save(any(Order.class))).thenAnswer(call -> Mono.just(call.getArgument(0)));

    StepVerifier.create(controller.create(
            jwt, new OrderController.CreateOrder(new BigDecimal("10.50"), "USD")))
        .assertNext(saved -> {
          assertThat(saved.customerId()).isEqualTo("customer-123");
          assertThat(saved.amount()).isEqualByComparingTo("10.50");
          assertThat(saved.currency()).isEqualTo("USD");
          assertThat(saved.status()).isEqualTo("CREATED");
        }).verifyComplete();

    var captor = ArgumentCaptor.forClass(Order.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().customerId()).isEqualTo("customer-123");
  }

  @Test
  void rejectsZeroOrderAmount() {
    try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
      var violations = validatorFactory.getValidator()
          .validate(new OrderController.CreateOrder(BigDecimal.ZERO, "USD"));
      assertThat(violations).extracting(v -> v.getPropertyPath().toString())
          .containsExactly("amount");
    }
  }

  @Test
  void rejectsExcessiveAmountAndUnsupportedCurrency() {
    try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
      var violations = validatorFactory.getValidator().validate(
          new OrderController.CreateOrder(new BigDecimal("1000000.01"), "EUR"));

      assertThat(violations).extracting(v -> v.getPropertyPath().toString())
          .containsExactlyInAnyOrder("amount", "currency");
    }
  }

  @Test
  void rejectsNegativeAndHighPrecisionAmounts() {
    try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
      var validator = validatorFactory.getValidator();

      var negative = validator.validate(
          new OrderController.CreateOrder(new BigDecimal("-1.00"), "USD"));
      var highPrecision = validator.validate(
          new OrderController.CreateOrder(new BigDecimal("10.123"), "KHR"));

      assertThat(negative).extracting(v -> v.getPropertyPath().toString())
          .contains("amount");
      assertThat(highPrecision).extracting(v -> v.getPropertyPath().toString())
          .contains("amount");
    }
  }
}
