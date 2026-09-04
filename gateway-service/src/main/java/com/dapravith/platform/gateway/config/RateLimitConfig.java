package com.dapravith.platform.gateway.config;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
@Configuration
public class RateLimitConfig {
  @Bean
  KeyResolver principalKeyResolver() {
    return exchange -> exchange.getPrincipal()
        .flatMap(principal -> Mono.justOrEmpty(principal.getName()))
        .switchIfEmpty(Mono.just("anonymous"));
  }
}
