package com.ling.lingaicodegeneration.model.dto.rag;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    // 用户当前问题
    private String question;
    // metadata filtering（可选）
    private String docType;
    private String symptomTag;
    // 对话历史（可选）
    private List<ChatMessage> history;
}