package com.dapravith.platform.order.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import com.dapravith.platform.order.config.SecurityConfig;
import com.dapravith.platform.order.domain.Order;
import com.dapravith.platform.order.domain.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = OrderController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class OrderSecurityWebTest {
  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private OrderRepository repository;

  @MockBean
  private ReactiveJwtDecoder jwtDecoder;

  @Test
  void rejectsAnonymousRequest() {
    webTestClient.get().uri("/api/orders")
        .exchange()
        .expectStatus().isUnauthorized()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.type").isEqualTo("urn:problem-type:unauthorized")
        .jsonPath("$.code").isEqualTo("unauthorized");
  }

  @Test
  void rejectsTokenWithoutReadScope() {
    webTestClient.mutateWith(mockJwt()
            .jwt(jwt -> jwt.subject("customer-123"))
            .authorities(new SimpleGrantedAuthority("SCOPE_profile.read")))
        .get().uri("/api/orders")
        .exchange()
        .expectStatus().isForbidden()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.type").isEqualTo("urn:problem-type:forbidden")
        .jsonPath("$.code").isEqualTo("forbidden");
  }

  @Test
  void rejectsTokenWithoutWriteScope() {
    webTestClient.mutateWith(mockJwt()
            .jwt(jwt -> jwt.subject("customer-123"))
            .authorities(new SimpleGrantedAuthority("SCOPE_orders.read")))
        .post().uri("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of("amount", "10.50", "currency", "USD"))
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  void readsOnlyOrdersOwnedByJwtSubject() {
    var order = new Order(
        UUID.randomUUID(),
        "customer-123",
        new BigDecimal("25.00"),
        "USD",
        "CREATED",
        Instant.now(),
        0L);
    when(repository.findAllByCustomerIdOrderByCreatedAtDesc("customer-123"))
        .thenReturn(Flux.just(order));

    webTestClient.mutateWith(mockJwt()
            .jwt(jwt -> jwt.subject("customer-123"))
            .authorities(new SimpleGrantedAuthority("SCOPE_orders.read")))
        .get().uri("/api/orders")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].customerId").isEqualTo("customer-123")
        .jsonPath("$[0].currency").isEqualTo("USD");
  }

  @Test
  void createsOrderWithSupportedCurrency() {
    when(repository.save(any(Order.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

    webTestClient.mutateWith(mockJwt()
            .jwt(jwt -> jwt.subject("customer-123"))
            .authorities(new SimpleGrantedAuthority("SCOPE_orders.write")))
        .post().uri("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of("amount", "10.50", "currency", "USD"))
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.customerId").isEqualTo("customer-123")
        .jsonPath("$.amount").isEqualTo(10.50)
        .jsonPath("$.currency").isEqualTo("USD")
        .jsonPath("$.status").isEqualTo("CREATED");
  }

  @Test
  void returnsProblemDetailsForInvalidAmountAndCurrency() {
    webTestClient.mutateWith(mockJwt()
            .jwt(jwt -> jwt.subject("customer-123"))
            .authorities(new SimpleGrantedAuthority("SCOPE_orders.write")))
        .post().uri("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(Map.of("amount", "1000000.01", "currency", "usd"))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.type").isEqualTo("urn:problem-type:validation")
        .jsonPath("$.title").isEqualTo("Request validation failed")
        .jsonPath("$.status").isEqualTo(400)
        .jsonPath("$.code").isEqualTo("validation_failed")
        .jsonPath("$.violations").isArray();
  }

  @Test
  void returnsProblemDetailsForMalformedJson() {
    webTestClient.mutateWith(mockJwt()
            .jwt(jwt -> jwt.subject("customer-123"))
            .authorities(new SimpleGrantedAuthority("SCOPE_orders.write")))
        .post().uri("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{not-json")
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.type").isEqualTo("urn:problem-type:invalid-request")
        .jsonPath("$.code").isEqualTo("invalid_request");
  }
}
