package com.dapravith.platform.gateway.config;

import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = SecurityConfigWebTest.ProbeController.class)
@Import({SecurityConfig.class, SecurityConfigWebTest.ProbeController.class})
class SecurityConfigWebTest {
  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private ReactiveJwtDecoder jwtDecoder;

  @Test
  void rejectsAnonymousRequest() {
    webTestClient.get().uri("/probe")
        .exchange()
        .expectStatus().isUnauthorized()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .expectBody()
        .jsonPath("$.type").isEqualTo("urn:problem-type:unauthorized")
        .jsonPath("$.code").isEqualTo("unauthorized");
  }

  @Test
  void acceptsValidBearerToken() {
    when(jwtDecoder.decode("valid-token")).thenReturn(Mono.just(jwt()));

    webTestClient.get().uri("/probe")
        .headers(headers -> headers.setBearerAuth("valid-token"))
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class).isEqualTo("ok");
  }

  private Jwt jwt() {
    Instant now = Instant.now();
    return Jwt.withTokenValue("valid-token")
        .header("alg", "RS256")
        .issuer("https://identity.example.test/realms/platform")
        .subject("customer-123")
        .audience(List.of("platform-api"))
        .issuedAt(now.minusSeconds(30))
        .expiresAt(now.plusSeconds(300))
        .build();
  }

  @RestController
  public static class ProbeController {
    @GetMapping("/probe")
    public Mono<String> probe() {
      return Mono.just("ok");
    }
  }
}
