package com.dapravith.platform.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class JwtDecoderConfig {
  @Bean
  ReactiveJwtDecoder jwtDecoder(
      @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
      @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${platform.security.jwt.audience}") String audience) {
    var decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    decoder.setJwtValidator(validator(issuer, audience));
    return decoder;
  }

  static OAuth2TokenValidator<Jwt> validator(String issuer, String audience) {
    return new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefaultWithIssuer(issuer), new AudienceValidator(audience));
  }

  private record AudienceValidator(String audience) implements OAuth2TokenValidator<Jwt> {
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
      if (jwt.getAudience() != null && jwt.getAudience().contains(audience)) {
        return OAuth2TokenValidatorResult.success();
      }

      return OAuth2TokenValidatorResult.failure(new OAuth2Error(
          OAuth2ErrorCodes.INVALID_TOKEN,
          "The token does not contain the required audience",
          null));
    }
  }
}
