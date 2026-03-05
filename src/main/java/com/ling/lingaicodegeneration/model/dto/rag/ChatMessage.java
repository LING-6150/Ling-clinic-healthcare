package com.ling.lingaicodegeneration.model.dto.rag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String role;    // "user" 或 "assistant"
    private String content;
}