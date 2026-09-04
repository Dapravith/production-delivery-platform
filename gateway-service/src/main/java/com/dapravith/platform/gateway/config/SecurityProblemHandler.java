package com.dapravith.platform.gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class SecurityProblemHandler
    implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {
  private final ObjectMapper objectMapper;

  public SecurityProblemHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
    return write(exchange, HttpStatus.UNAUTHORIZED, "Authentication required", "unauthorized");
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException exception) {
    return write(exchange, HttpStatus.FORBIDDEN, "Access denied", "forbidden");
  }

  private Mono<Void> write(
      ServerWebExchange exchange, HttpStatus status, String title, String code) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, title);
    problem.setType(URI.create("urn:problem-type:" + code));
    problem.setTitle(title);
    problem.setInstance(URI.create(exchange.getRequest().getPath().value()));
    problem.setProperty("code", code);

    try {
      byte[] body = objectMapper.writeValueAsBytes(problem);
      exchange.getResponse().setStatusCode(status);
      exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
      return exchange.getResponse().writeWith(
          Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    } catch (JsonProcessingException error) {
      return Mono.error(new IllegalStateException("Could not serialize security problem", error));
    }
  }
}
