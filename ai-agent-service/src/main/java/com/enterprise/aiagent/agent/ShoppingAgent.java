package com.enterprise.aiagent.agent;

import com.enterprise.aiagent.tool.InventoryTool;
import com.enterprise.aiagent.tool.ProductTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Specialized agent for product discovery, recommendations, and inventory queries.
 *
 * This agent has access to:
 * - ProductTool: search products, get details, browse categories
 * - InventoryTool: check stock levels
 *
 * The agentic loop works as follows:
 * 1. User asks "Find me a good laptop under $1000"
 * 2. Agent calls searchProducts("laptop") → gets list
 * 3. Agent filters by price, calls getProductDetails() for top candidates
 * 4. Agent calls checkStock() for the best matches
 * 5. Agent synthesizes all information into a recommendation
 *
 * Spring AI handles this loop automatically — the agent just needs
 * good tools and clear descriptions.
 */
@Component
public class ShoppingAgent extends BaseAgent {

    public ShoppingAgent(@Qualifier("shoppingChatClient") ChatClient shoppingChatClient) {
        super(shoppingChatClient);
    }

    @Override
    public String getAgentName() {
        return "ShoppingAgent";
    }
}