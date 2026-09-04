package com.dapravith.platform.order.web;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class ApiExceptionHandler {
  private static final URI VALIDATION_TYPE = URI.create("urn:problem-type:validation");
  private static final URI INVALID_REQUEST_TYPE = URI.create("urn:problem-type:invalid-request");

  @ExceptionHandler(WebExchangeBindException.class)
  ResponseEntity<ProblemDetail> validationFailure(
      WebExchangeBindException exception, ServerWebExchange exchange) {
    List<Violation> violations = exception.getFieldErrors().stream()
        .sorted(Comparator.comparing(error -> error.getField()))
        .map(error -> new Violation(
            error.getField(),
            error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage()))
        .toList();

    ProblemDetail problem = problem(
        HttpStatus.BAD_REQUEST,
        VALIDATION_TYPE,
        "Request validation failed",
        "One or more request fields are invalid.",
        "validation_failed",
        exchange);
    problem.setProperty("violations", violations);
    return response(problem);
  }

  @ExceptionHandler(ServerWebInputException.class)
  ResponseEntity<ProblemDetail> invalidRequest(
      ServerWebInputException exception, ServerWebExchange exchange) {
    ProblemDetail problem = problem(
        HttpStatus.BAD_REQUEST,
        INVALID_REQUEST_TYPE,
        "Invalid request",
        "The request body or parameters could not be read.",
        "invalid_request",
        exchange);
    return response(problem);
  }

  private ProblemDetail problem(
      HttpStatus status,
      URI type,
      String title,
      String detail,
      String code,
      ServerWebExchange exchange) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setType(type);
    problem.setTitle(title);
    problem.setInstance(URI.create(exchange.getRequest().getPath().value()));
    problem.setProperty("code", code);
    return problem;
  }

  private ResponseEntity<ProblemDetail> response(ProblemDetail problem) {
    return ResponseEntity.status(problem.getStatus())
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }

  public record Violation(String field, String message) {}
}
