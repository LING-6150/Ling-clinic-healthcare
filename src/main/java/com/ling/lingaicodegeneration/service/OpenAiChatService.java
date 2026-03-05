package com.ling.lingaicodegeneration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class OpenAiChatService {

    @Value("${openai.api-key}")
    private String apiKey;

    private static final String CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 流式调用，每收到一个token就调用onToken回调
    public void streamChat(List<Map<String, String>> messages,
                           Consumer<String> onToken,
                           Runnable onComplete) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "messages", messages,
                    "stream", true
            );

            Request request = new Request.Builder()
                    .url(CHAT_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(
                            objectMapper.writeValueAsString(requestBody),
                            MediaType.parse("application/json")))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    onToken.accept("[ERROR] " + e.getMessage());
                    onComplete.run();
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    try (ResponseBody body = response.body()) {
                        if (body == null) {
                            onComplete.run();
                            return;
                        }
                        // 逐行读取SSE流
                        okio.BufferedSource source = body.source();
                        while (!source.exhausted()) {
                            String line = source.readUtf8Line();
                            if (line == null) break;
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if (data.equals("[DONE]")) {
                                    onComplete.run();
                                    break;
                                }
                                try {
                                    JsonNode node = objectMapper.readTree(data);
                                    JsonNode delta = node
                                            .path("choices").get(0)
                                            .path("delta")
                                            .path("content");
                                    if (!delta.isMissingNode() && !delta.isNull()) {
                                        onToken.accept(delta.asText());
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to call OpenAI: " + e.getMessage());
        }
    }
}