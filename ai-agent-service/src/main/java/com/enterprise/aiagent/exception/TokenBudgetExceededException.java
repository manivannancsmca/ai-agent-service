package com.enterprise.aiagent.exception;

public class TokenBudgetExceededException extends AgentException {

    public TokenBudgetExceededException(String message) {
        super(message, "TOOL_EXECUTION_ERROR");
    }

    public TokenBudgetExceededException(String message, Throwable cause) {
        super(message, "TOOL_EXECUTION_ERROR", cause);
    }
}
