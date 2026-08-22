package com.enterprise.aiagent.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AgentException.class)
    public ProblemDetail handleAgentException(AgentException ex) {
        log.error("Agent error [{}]: {}", ex.getErrorCode(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setTitle("AI Agent Error");
        problem.setType(URI.create("https://api.enterprise.com/errors/" + ex.getErrorCode()));
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimit(RateLimitExceededException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problem.setTitle("Rate Limit Exceeded");
        problem.setProperty("retryAfterSeconds", 30);
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed: " + errors);
        problem.setTitle("Validation Error");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        problem.setTitle("Internal Server Error");
        return problem;
    }
}