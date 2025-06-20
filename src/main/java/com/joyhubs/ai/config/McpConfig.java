package com.joyhubs.ai.config;

import com.joyhubs.ai.service.BookingTools;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP配置类
 *
 * @author OxNPC
 */
@Configuration
public class McpConfig {

    /**
     * 对话上下文
     *
     * @return
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(50)
                .build();
    }

    /**
     * 将 ChatClient 定义为一个 Spring Bean，以便 MCP 服务器可以自动发现它。
     * MCP 服务器会检查此 Bean 的默认工具、系统提示等信息，并将它们暴露给客户端。
     *
     * @param chatClientBuilder Spring Boot 自动配置的 ChatClient.Builder
     * @param bookingTools      您定义的工具 Bean
     * @param vectorStore       向量存储 Bean
     * @param chatMemory        聊天记忆 Bean
     * @return 配置好的 ChatClient 实例
     */
    @Bean
    public ChatClient chatClient(
            ChatClient.Builder chatClientBuilder,
            BookingTools bookingTools,
            VectorStore vectorStore,
            ChatMemory chatMemory
    ) {
        return chatClientBuilder
                .defaultSystem("""
                            You are a customer chat support agent of an airline named "Funnair".
                            Respond in a friendly, helpful, and joyful manner.
                            You are interacting with customers through an online chat system.
                        
                            请使用中文进行所有交互，除非用户明确要求使用其他语言。
                        
                            When the user asks to see 'all' or 'a list of all' available bookings, you can use the 'getAllBookings' function without requiring any parameters.
                        
                            For other booking-related queries, such as getting details of a specific booking, changing a booking, or cancelling a booking, you MUST always
                            get the following information from the user: booking number, customer first name and last name.
                            If you can not retrieve the status of a specific flight, please just say "I am sorry, I can not find the booking details".
                            Check the message history for booking details before asking the user for specific booking operations.
                        
                            Before changing a booking you MUST ensure it is permitted by the terms.
                            If there is a charge for the change, you MUST ask the user to consent before proceeding.
                            Use the provided functions to fetch booking details, change bookings, and cancel bookings, and to list all bookings.
                        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore).build()
                )
                // 在这里注册的工具可以被 MCP 服务器发现
                .defaultTools(bookingTools)
                .defaultOptions(
                        VertexAiGeminiChatOptions.builder()
                                .internalToolExecutionEnabled(true)
                                .build()
                )
                .build();
    }

    /**
     * Tools注册暴露给客户端
     *
     * @param bookingTools
     * @return
     */
    @Bean
    public ToolCallbackProvider bookTools(BookingTools bookingTools) {
        return MethodToolCallbackProvider.builder().toolObjects(bookingTools).build();
    }


    @Bean
    public McpServerFeatures.AsyncPromptSpecification samplePromptSpecification() {
        McpSchema.Prompt prompt = new McpSchema.Prompt("sample-prompt", "A sample prompt for demonstration",
                List.of(
                        new McpSchema.PromptArgument("question", "The topic to generate content about", false)
                )
        );
        // 创建 Handler
        BiFunction<McpAsyncServerExchange, McpSchema.GetPromptRequest, Mono<McpSchema.GetPromptResult>> handler =
                (exchange, request) -> {
                    try {
                        // 获取请求参数
                        Map<String, Object> arguments = request.arguments();
                        String question = (String) arguments.get("question");
                        // 生成 prompt 内容
                        String promptContent = String.format("Could you help me handle the problem of the bookings ? My question is: %s.", question);
                        // 构建返回结果
                        McpSchema.GetPromptResult result = new McpSchema.GetPromptResult("sample-prompt details",
                                List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(promptContent)))
                        );
                        return Mono.just(result);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to generate prompt: " + e.getMessage(), e));
                    }
                };
        return new McpServerFeatures.AsyncPromptSpecification(prompt, handler);
    }

}
