package com.ling.lingaicodegeneration.mapper;

import com.ling.lingaicodegeneration.model.entity.DocumentChunk;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunk> {

    // 插入chunk和embedding（原生SQL）
    void insertChunkWithEmbedding(@Param("chunk") DocumentChunk chunk,
                                  @Param("embedding") String embeddingJson);

    // 向量相似度检索
    List<DocumentChunk> vectorSearch(@Param("embedding") String embeddingJson,
                                     @Param("limit") int limit);

    // 全文检索
    List<DocumentChunk> keywordSearch(@Param("keyword") String keyword,
                                      @Param("limit") int limit);

    // Hybrid search - 带metadata filtering的向量检索
    List<DocumentChunk> vectorSearchWithFilter(
            @Param("embedding") String embeddingJson,
            @Param("docType") String docType,
            @Param("symptomTag") String symptomTag,
            @Param("limit") int limit);

    // Hybrid search - 带metadata filtering的关键词检索
    List<DocumentChunk> keywordSearchWithFilter(
            @Param("keyword") String keyword,
            @Param("docType") String docType,
            @Param("symptomTag") String symptomTag,
            @Param("limit") int limit);
}