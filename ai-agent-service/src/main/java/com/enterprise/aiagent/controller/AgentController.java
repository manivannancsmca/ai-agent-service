package com.enterprise.aiagent.controller;

import com.enterprise.aiagent.model.dto.ChatRequest;
import com.enterprise.aiagent.model.dto.ChatResponse;
import com.enterprise.aiagent.service.AgentOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

//@Slf4j
@RestController
@RequestMapping("/api/v1/agent")
//@RequiredArgsConstructor
public class AgentController {

    	private static final Logger log =
	            LoggerFactory.getLogger(AgentController.class);

    private final AgentOrchestrator orchestrator;

    public AgentController(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Main chat endpoint. Sends a message to the AI agent and receives a response.
     *
     * The agent uses an agentic loop (tool calling) to fulfill complex requests.
     * It may call multiple tools across multiple microservices before responding.
     *
     * @param request The chat message with optional conversation context
     * @return The agent's response with metadata
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("POST /api/v1/agent/chat — userId={}, conversationId={}",
                request.userId(), request.conversationId());

        ChatResponse response = orchestrator.processChat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Streaming chat endpoint using Server-Sent Events (SSE).
     *
     * Sends the response in chunks for real-time UI updates.
     * Ideal for chat interfaces that want to show the response being typed.
     *
     * @param request The chat message
     * @return A stream of text chunks
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody ChatRequest request) {
        log.info("POST /api/v1/agent/chat/stream — userId={}", request.userId());
        return orchestrator.streamChat(request);
    }

    /**
     * Health check endpoint specific to the AI agent.
     * Reports whether the agent is ready to accept requests.
     */
    @GetMapping("/health")
    public ResponseEntity<AgentHealthResponse> health() {
        return ResponseEntity.ok(new AgentHealthResponse(
                "UP",
                "AI Agent is ready",
                System.currentTimeMillis()
        ));
    }

    record AgentHealthResponse(String status, String message, long timestamp) {}
}