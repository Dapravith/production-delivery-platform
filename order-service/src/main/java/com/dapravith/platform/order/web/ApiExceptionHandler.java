package com.dapravith.platform.order.web;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<Map<String, Object>> validationFailure(
      WebExchangeBindException exception, ServerWebExchange exchange) {
    List<Violation> violations = exception.getFieldErrors().stream()
        .sorted(Comparator.comparing(error -> error.getField()))
        .map(error -> new Violation(
            error.getField(),
            error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage()))
        .toList();

    Map<String, Object> problem = problem(
        HttpStatus.BAD_REQUEST,
        "urn:problem-type:validation",
        "Request validation failed",
        "One or more request fields are invalid.",
        "validation_failed",
        exchange);
    problem.put("violations", violations);
    return response(problem);
  }

  @ExceptionHandler(ServerWebInputException.class)
  public ResponseEntity<Map<String, Object>> invalidRequest(
      ServerWebInputException exception, ServerWebExchange exchange) {
    Map<String, Object> problem = problem(
        HttpStatus.BAD_REQUEST,
        "urn:problem-type:invalid-request",
        "Invalid request",
        "The request body or parameters could not be read.",
        "invalid_request",
        exchange);
    return response(problem);
  }

  private Map<String, Object> problem(
      HttpStatus status,
      String type,
      String title,
      String detail,
      String code,
      ServerWebExchange exchange) {
    Map<String, Object> problem = new LinkedHashMap<>();
    problem.put("type", type);
    problem.put("title", title);
    problem.put("status", status.value());
    problem.put("detail", detail);
    problem.put("instance", exchange.getRequest().getPath().value());
    problem.put("code", code);
    return problem;
  }

  private ResponseEntity<Map<String, Object>> response(Map<String, Object> problem) {
    return ResponseEntity.status((Integer) problem.get("status"))
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }

  public record Violation(String field, String message) {}
}
