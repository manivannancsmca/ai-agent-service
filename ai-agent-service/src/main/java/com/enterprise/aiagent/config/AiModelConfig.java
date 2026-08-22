package com.enterprise.aiagent.config;


import com.enterprise.aiagent.advisor.GuardrailAdvisor;
import com.enterprise.aiagent.advisor.LoggingAdvisor;
import com.enterprise.aiagent.advisor.TokenBudgetAdvisor;
import com.enterprise.aiagent.memory.JpaChatMemory;
import com.enterprise.aiagent.tool.InventoryTool;
import com.enterprise.aiagent.tool.OrderTool;
import com.enterprise.aiagent.tool.PaymentTool;
import com.enterprise.aiagent.tool.ProductTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;


@Configuration
public class AiModelConfig {

    /**
     * Shopping Agent ChatClient.
     * Specializes in product discovery, recommendations, and inventory queries.
     * Has access to ProductTool and InventoryTool.
     */
    @Bean
    public ChatClient shoppingChatClient(
            ChatModel chatModel,
            JpaChatMemory chatMemory,
            ProductTool productTool,
            InventoryTool inventoryTool,
            LoggingAdvisor loggingAdvisor,
            GuardrailAdvisor guardrailAdvisor,
            TokenBudgetAdvisor tokenBudgetAdvisor,
            @Value("classpath:prompts/shopping-system.st") Resource shoppingSystemPrompt) {

        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem(shoppingSystemPrompt)
                .defaultTools(productTool, inventoryTool)
                .defaultAdvisors(memoryAdvisor, guardrailAdvisor, tokenBudgetAdvisor, loggingAdvisor)
                .defaultOptions(org.springframework.ai.openai.OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .temperature(0.7)
                        .build())
                .build();
    }

    /**
     * Order Agent ChatClient.
     * Specializes in order lifecycle management, payment processing.
     * Has access to OrderTool and PaymentTool.
     */
    @Bean
    public ChatClient orderChatClient(
            ChatModel chatModel,
            JpaChatMemory chatMemory,
            OrderTool orderTool,
            PaymentTool paymentTool,
            LoggingAdvisor loggingAdvisor,
            GuardrailAdvisor guardrailAdvisor,
            TokenBudgetAdvisor tokenBudgetAdvisor,
            @Value("classpath:prompts/order-system.st") Resource orderSystemPrompt) {

        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem(orderSystemPrompt)
                .defaultTools(orderTool, paymentTool)
                .defaultAdvisors(memoryAdvisor, guardrailAdvisor, tokenBudgetAdvisor, loggingAdvisor)
                .defaultOptions(org.springframework.ai.openai.OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .temperature(0.3) // Lower temperature for order operations (precision)
                        .build())
                .build();
    }

    /**
     * Supervisor Agent ChatClient.
     * Routes requests to the appropriate specialist agent.
     * No direct tools — it orchestrates via the AgentOrchestrator.
     */
    @Bean
    public ChatClient supervisorChatClient(
            ChatModel chatModel,
            JpaChatMemory chatMemory,
            LoggingAdvisor loggingAdvisor,
            GuardrailAdvisor guardrailAdvisor,
            @Value("classpath:prompts/supervisor-system.st") Resource supervisorSystemPrompt) {

        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem(supervisorSystemPrompt)
                .defaultAdvisors(memoryAdvisor, guardrailAdvisor, loggingAdvisor)
                .defaultOptions(org.springframework.ai.openai.OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .temperature(0.2) // Deterministic routing
                        .build())
                .build();
    }
}