package com.enterprise.aiagent.exception;

/**
 * Base exception for all AI agent errors.
 */
public class AgentException extends RuntimeException {
    private final String errorCode;

    public AgentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AgentException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}