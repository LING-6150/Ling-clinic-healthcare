package com.ling.lingaicodegeneration.controller;

import com.ling.lingaicodegeneration.model.dto.rag.ChatRequest;
import com.ling.lingaicodegeneration.service.RagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/rag")
public class RagController {

    @Resource
    private RagService ragService;

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody ChatRequest request) {
        return ragService.chat(request);
    }
}