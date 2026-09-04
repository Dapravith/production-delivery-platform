package com.dapravith.platform.gateway.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
@Configuration @EnableWebFluxSecurity
public class SecurityConfig {
  @Bean SecurityWebFilterChain security(ServerHttpSecurity http) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
      .authorizeExchange(a -> a.pathMatchers("/actuator/health/**", "/actuator/prometheus").permitAll().anyExchange().authenticated())
      .oauth2ResourceServer(o -> o.jwt(j -> {})).build();
  }
}
