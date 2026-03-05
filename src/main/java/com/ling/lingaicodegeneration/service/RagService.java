package com.ling.lingaicodegeneration.service;

import com.ling.lingaicodegeneration.model.dto.rag.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RagService {
    // SSE流式问答
    SseEmitter chat(ChatRequest request);
}