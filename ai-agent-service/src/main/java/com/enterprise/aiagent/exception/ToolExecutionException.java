package com.enterprise.aiagent.exception;

/**
 * Thrown when a tool fails to execute.
 * The error message is crafted to be useful to the LLM
 * so it can inform the user or retry with different parameters.
 */
public class ToolExecutionException extends AgentException {

    public ToolExecutionException(String message) {
        super(message, "TOOL_EXECUTION_ERROR");
    }

    public ToolExecutionException(String message, Throwable cause) {
        super(message, "TOOL_EXECUTION_ERROR", cause);
    }
}