package com.ling.lingaicodegeneration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.embedding-url}")
    private String embeddingUrl;

    @Value("${openai.embedding-model}")
    private String embeddingModel;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 获取单个文本的embedding，返回JSON数组字符串 "[0.1, 0.2, ...]"
    public String getEmbedding(String text) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", embeddingModel,
                    "input", text
            ));
            Request request = new Request.Builder()
                    .url(embeddingUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody,
                            MediaType.parse("application/json")))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode embeddingArray = root.path("data").get(0).path("embedding");
                // 返回 "[0.1,0.2,...]" 格式
                return embeddingArray.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get embedding: " + e.getMessage());
        }
    }

    // 批量获取embedding
    public List<String> getEmbeddingsBatch(List<String> texts) {
        List<String> results = new ArrayList<>();
        for (String text : texts) {
            results.add(getEmbedding(text));
        }
        return results;
    }
}