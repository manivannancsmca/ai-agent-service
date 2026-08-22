package com.enterprise.aiagent.agent;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Supervisor Agent that routes incoming requests to the appropriate specialist.
 *
 * Architecture:
 *   User → Supervisor → ShoppingAgent (products, inventory)
 *                      → OrderAgent (orders, payments)
 *
 * The Supervisor uses an LLM to classify the user's intent, then delegates
 * to the specialist agent. For complex requests that span multiple domains
 * (e.g., "find a laptop and order it"), the Supervisor orchestrates a
 * multi-step flow across agents.
 *
 * This is the core of the multi-agent architecture.
 */
@Component
public class SupervisorAgent extends BaseAgent {

    private final ShoppingAgent shoppingAgent;
    private final OrderAgent orderAgent;

    public SupervisorAgent(
            @Qualifier("supervisorChatClient") ChatClient supervisorChatClient,
            ShoppingAgent shoppingAgent,
            OrderAgent orderAgent) {
        super(supervisorChatClient);
        this.shoppingAgent = shoppingAgent;
        this.orderAgent = orderAgent;
    }

    /**
     * Main entry point: routes the user message to the appropriate agent.
     *
     * Uses the LLM to classify intent, then delegates. If the LLM-based
     * classification fails, falls back to keyword-based routing.
     */
    public String processRequest(String conversationId, String userMessage) {
        try {
            // Step 1: Ask the supervisor LLM to classify the intent
            IntentClassification classification = classifyIntent(conversationId, userMessage);

            // Step 2: Route to the appropriate agent
            return switch (classification.domain()) {
                case "shopping" -> shoppingAgent.chat(conversationId, userMessage);
                case "orders" -> orderAgent.chat(conversationId, userMessage);
                case "general" -> chatWithSupervisor(conversationId, userMessage);
                default -> delegateWithOrchestration(conversationId, userMessage, classification);
            };

        } catch (Exception e) {
            // Fallback: try keyword-based routing
            return fallbackRoute(conversationId, userMessage);
        }
    }

    /**
     * Ask the supervisor LLM to classify the user's intent.
     */
    private IntentClassification classifyIntent(String conversationId, String userMessage) {
        String classificationPrompt = """
                Analyze the following user message and classify it into exactly ONE domain.

                Domains:
                - "shopping": Product search, recommendations, browsing, inventory checks
                - "orders": Order placement, tracking, cancellation, payment, refunds
                - "general": Greetings, help requests, questions about capabilities,
                             anything that doesn't fit the above

                For multi-step requests (e.g., "find a product AND order it"),
                use "orchestrate" as the domain.

                Respond ONLY with a JSON object:
                {"domain": "<domain>", "reason": "<brief explanation>"}

                User message: """ + userMessage;

        try {
            String result = chatClient.prompt()
                    .user(classificationPrompt)
                    .advisors(a -> a.param("conversation_id", conversationId))
                    .call()
                    .content();

            // Parse the JSON response
            String domain = extractDomain(result);
            return new IntentClassification(domain, result);

        } catch (Exception e) {
            return new IntentClassification("general", "classification failed");
        }
    }

    /**
     * Handle complex multi-domain requests by orchestrating across agents.
     *
     * Example: "Find me a gaming laptop under $1500 and place an order"
     * 1. ShoppingAgent finds products
     * 2. ShoppingAgent provides recommendation
     * 3. OrderAgent places the order
     */
    private String delegateWithOrchestration(String conversationId, String userMessage,
                                              IntentClassification classification) {
        // For orchestration, we first handle shopping, then pass context to orders
        String shoppingResponse = shoppingAgent.chat(conversationId, userMessage);

        // Check if the response indicates an order should be placed
        // The supervisor's system prompt guides this decision
        String followUpPrompt = """
                The user asked: %s

                The shopping assistant responded:
                %s

                Based on this, does the user want to proceed with an order?
                If yes, extract the product ID and quantity and respond with:
                {"action": "order", "productId": <id>, "quantity": <qty>}

                If no, respond with:
                {"action": "none"}
                """.formatted(userMessage, shoppingResponse);

        try {
            String decision = chatClient.prompt()
                    .user(followUpPrompt)
                    .advisors(a -> a.param("conversation_id", conversationId))
                    .call()
                    .content();

            if (decision.contains("\"action\": \"order\"")) {
                // Delegate to order agent
                String orderResponse = orderAgent.chat(conversationId,
                        "Based on our previous conversation, please proceed with the order.");
                return shoppingResponse + "\n\n---\n\n" + orderResponse;
            }
        } catch (Exception e) {
            // If orchestration fails, return the shopping response at minimum
        }

        return shoppingResponse;
    }

    private String chatWithSupervisor(String conversationId, String userMessage) {
        return chat(conversationId, userMessage);
    }

    /**
     * Keyword-based fallback when LLM classification fails.
     */
    private String fallbackRoute(String conversationId, String userMessage) {
        String lower = userMessage.toLowerCase();

        if (containsAny(lower, "product", "search", "find", "buy", "recommend",
                "price", "catalog", "browse", "inventory", "stock", "available")) {
            return shoppingAgent.chat(conversationId, userMessage);
        }

        if (containsAny(lower, "order", "track", "cancel", "payment", "refund",
                "deliver", "ship", "purchase")) {
            return orderAgent.chat(conversationId, userMessage);
        }

        // Default: handle as general query
        return chat(conversationId, userMessage);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private String extractDomain(String classificationJson) {
        // Simple extraction — in production, use proper JSON parsing
        if (classificationJson.contains("\"shopping\"")) return "shopping";
        if (classificationJson.contains("\"orders\"")) return "orders";
        if (classificationJson.contains("\"orchestrate\"")) return "orchestrate";
        return "general";
    }

    @Override
    public String getAgentName() {
        return "SupervisorAgent";
    }

    record IntentClassification(String domain, String reason) {}
}