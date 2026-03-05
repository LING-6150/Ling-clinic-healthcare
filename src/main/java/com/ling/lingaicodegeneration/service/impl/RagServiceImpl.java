package com.ling.lingaicodegeneration.service.impl;

import com.ling.lingaicodegeneration.mapper.DocumentChunkMapper;
import com.ling.lingaicodegeneration.model.dto.rag.ChatMessage;
import com.ling.lingaicodegeneration.model.dto.rag.ChatRequest;
import com.ling.lingaicodegeneration.model.entity.DocumentChunk;
import com.ling.lingaicodegeneration.service.EmbeddingService;
import com.ling.lingaicodegeneration.service.OpenAiChatService;
import com.ling.lingaicodegeneration.service.RagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagServiceImpl implements RagService {

    @Resource
    private EmbeddingService embeddingService;

    @Resource
    private DocumentChunkMapper documentChunkMapper;

    @Resource
    private OpenAiChatService openAiChatService;

    private static final int SEARCH_LIMIT = 5;
    private static final int TOP_K = 3;

    @Override
    public SseEmitter chat(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(60_000L);

        // 异步处理，不阻塞主线程
        new Thread(() -> {
            try {
                // 1. 获取问题的embedding
                String questionEmbedding = embeddingService.getEmbedding(request.getQuestion());

                // 2. Hybrid Search
                List<DocumentChunk> vectorResults = documentChunkMapper.vectorSearchWithFilter(
                        questionEmbedding,
                        request.getDocType(),
                        request.getSymptomTag(),
                        SEARCH_LIMIT
                );

                List<DocumentChunk> keywordResults = documentChunkMapper.keywordSearchWithFilter(
                        request.getQuestion(),
                        request.getDocType(),
                        request.getSymptomTag(),
                        SEARCH_LIMIT
                );

                // 3. RRF merge（Reciprocal Rank Fusion）
                List<DocumentChunk> mergedResults = rrfMerge(vectorResults, keywordResults, TOP_K);

                // 4. 构建context
                String context = mergedResults.stream()
                        .map(DocumentChunk::getContent)
                        .collect(Collectors.joining("\n\n"));

                // 5. 构建messages（含对话历史）
                List<Map<String, String>> messages = new ArrayList<>();

                // system prompt
                messages.add(Map.of(
                        "role", "system",
                        "content", "You are a professional Traditional Chinese Medicine (TCM) knowledge assistant. " +
                                "Answer questions based on the provided context. " +
                                "If the context doesn't contain relevant information, say so honestly.\n\n" +
                                "Context:\n" + context
                ));

                // 加入对话历史
                if (request.getHistory() != null) {
                    for (ChatMessage msg : request.getHistory()) {
                        messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                    }
                }

                // 当前用户问题
                messages.add(Map.of("role", "user", "content", request.getQuestion()));

                // 6. 流式调用OpenAI
                openAiChatService.streamChat(
                        messages,
                        token -> {
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                                emitter.complete();
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        }
                );

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    // RRF算法：把向量检索和关键词检索的结果合并，去重后按综合分数排序
    private List<DocumentChunk> rrfMerge(
            List<DocumentChunk> vectorResults,
            List<DocumentChunk> keywordResults,
            int topK) {

        Map<Long, Double> scores = new HashMap<>();
        Map<Long, DocumentChunk> chunkMap = new HashMap<>();

        // 向量检索结果打分
        for (int i = 0; i < vectorResults.size(); i++) {
            DocumentChunk chunk = vectorResults.get(i);
            scores.merge(chunk.getId(), 1.0 / (60 + i + 1), Double::sum);
            chunkMap.put(chunk.getId(), chunk);
        }

        // 关键词检索结果打分
        for (int i = 0; i < keywordResults.size(); i++) {
            DocumentChunk chunk = keywordResults.get(i);
            scores.merge(chunk.getId(), 1.0 / (60 + i + 1), Double::sum);
            chunkMap.put(chunk.getId(), chunk);
        }

        // 按分数降序排列，取前topK个
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topK)
                .map(e -> chunkMap.get(e.getKey()))
                .collect(Collectors.toList());
    }
}