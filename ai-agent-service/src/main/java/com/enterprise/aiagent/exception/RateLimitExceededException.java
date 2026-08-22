package com.enterprise.aiagent.exception;

public class RateLimitExceededException extends AgentException {

    public RateLimitExceededException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED");
    }
}