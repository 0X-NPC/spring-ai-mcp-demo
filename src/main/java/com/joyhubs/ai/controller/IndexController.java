package com.joyhubs.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 默认服务入口
 *
 * @author OxNPC
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class IndexController {

    private final ChatClient chatClient;

    /**
     * 聊天请求接口（流式响应）
     * <p>
     * MediaType.TEXT_EVENT_STREAM_VALUE 表示 MIME 类型是 Server-Sent Events (SSE)
     *
     * @param message 用户发送的消息
     * @return AI 生成的响应文本流
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam(value = "message", defaultValue = "Hi, what time is it?") String message,
                             @RequestParam(value = "chatId") String chatId) {
        // @formatter:off
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                 //.toolNames("getBookingDetails", "getAllBookings", "changeBooking", "cancelBooking")
                .stream()
                .content()
                .concatWith(Flux.just("[DONE]"))    // 主动标记结束
                .doOnNext(data -> log.info("Sending data chunk for {} : {}", chatId, data))     // 每发送一个数据块就打印
                .doOnComplete(() -> log.info("Flux completed successfully for {}", chatId))           // 流正常完成时打印
                .doOnError(e -> log.info("Backend: Flux encountered error for {}: {} ", chatId, e.getMessage()))            // 流出错时打印错误
                .doFinally(signalType -> log.info("Backend: Flux terminated with signal {} for {}", signalType, chatId))    // 流最终终止时打印终止类型
                .doOnCancel(() -> log.info("Backend: Flux cancelled (client disconnected) for {}", chatId));
        // @formatter:on
    }


}
