package com.dapravith.platform.gateway.config;

import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RateLimitConfigTest {
  private final RateLimitConfig config = new RateLimitConfig();

  @Test
  void usesAuthenticatedPrincipalAsRateLimitKey() {
    Principal principal = () -> "customer-123";
    var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders").build())
        .mutate().principal(Mono.just(principal)).build();

    StepVerifier.create(config.principalKeyResolver().resolve(exchange))
        .expectNext("customer-123").verifyComplete();
  }

  @Test
  void fallsBackToAnonymousKeyWhenNoPrincipalExists() {
    var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/actuator/health").build());

    StepVerifier.create(config.principalKeyResolver().resolve(exchange))
        .expectNext("anonymous").verifyComplete();
  }

  @Test
  void fallsBackToAnonymousKeyWhenPrincipalNameIsNull() {
    Principal principal = () -> null;
    var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders").build())
        .mutate().principal(Mono.just(principal)).build();

    StepVerifier.create(config.principalKeyResolver().resolve(exchange))
        .expectNext("anonymous").verifyComplete();
  }
}
