package com.dapravith.platform.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtDecoderConfigTest {
  private static final String ISSUER = "https://identity.example.test/realms/platform";
  private static final String AUDIENCE = "platform-api";

  @Test
  void acceptsExpectedIssuerAndAudience() {
    var result = JwtDecoderConfig.validator(ISSUER, AUDIENCE)
        .validate(jwt(ISSUER, List.of(AUDIENCE)));

    assertThat(result.hasErrors()).isFalse();
  }

  @Test
  void rejectsUnexpectedIssuer() {
    var result = JwtDecoderConfig.validator(ISSUER, AUDIENCE)
        .validate(jwt("https://attacker.example.test", List.of(AUDIENCE)));

    assertThat(result.hasErrors()).isTrue();
  }

  @Test
  void rejectsMissingAudience() {
    var result = JwtDecoderConfig.validator(ISSUER, AUDIENCE)
        .validate(jwt(ISSUER, null));

    assertThat(result.hasErrors()).isTrue();
  }

  @Test
  void rejectsExpiredToken() {
    Instant now = Instant.now();
    Jwt expired = Jwt.withTokenValue("expired-token")
        .header("alg", "RS256")
        .issuer(ISSUER)
        .subject("customer-123")
        .audience(List.of(AUDIENCE))
        .issuedAt(now.minusSeconds(600))
        .expiresAt(now.minusSeconds(300))
        .build();

    assertThat(JwtDecoderConfig.validator(ISSUER, AUDIENCE).validate(expired).hasErrors())
        .isTrue();
  }

  private Jwt jwt(String issuer, List<String> audience) {
    Instant now = Instant.now();
    var builder = Jwt.withTokenValue("test-token")
        .header("alg", "RS256")
        .issuer(issuer)
        .subject("customer-123")
        .issuedAt(now.minusSeconds(30))
        .expiresAt(now.plusSeconds(300));
    if (audience != null) {
      builder.audience(audience);
    }
    return builder.build();
  }
}
