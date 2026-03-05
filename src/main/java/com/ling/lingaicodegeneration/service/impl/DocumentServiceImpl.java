package com.ling.lingaicodegeneration.service.impl;

import com.ling.lingaicodegeneration.exception.BusinessException;
import com.ling.lingaicodegeneration.exception.ErrorCode;
import com.ling.lingaicodegeneration.mapper.DocumentChunkMapper;
import com.ling.lingaicodegeneration.mapper.DocumentMapper;
import com.ling.lingaicodegeneration.model.entity.Document;
import com.ling.lingaicodegeneration.model.entity.DocumentChunk;
import com.ling.lingaicodegeneration.model.entity.User;
import com.ling.lingaicodegeneration.model.vo.DocumentVO;
import com.ling.lingaicodegeneration.service.DocumentService;
import com.ling.lingaicodegeneration.service.EmbeddingService;
import com.ling.lingaicodegeneration.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document>
        implements DocumentService {

    @Resource
    private DocumentChunkMapper documentChunkMapper;

    @Resource
    private EmbeddingService embeddingService;

    @Resource
    private UserService userService;

    private static final int CHUNK_SIZE = 500;

    @Override
    public Long uploadDocument(MultipartFile file, String docType,
                               String symptomTag, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        // 1. 提取文本
        String text = extractText(file);

        // 2. SHA-256 去重
        String fileHash = sha256(text);
        QueryWrapper qw = QueryWrapper.create().eq("file_hash", fileHash);
        long exists = this.mapper.selectCountByQuery(qw);
        if (exists > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Document already exists");
        }

        // 3. 保存document元数据
        Document document = new Document();
        document.setTitle(file.getOriginalFilename());
        document.setDocType(docType);
        document.setFileHash(fileHash);
        document.setUploadUserId(loginUser.getId());
        this.save(document);

        // 第四步 文本分块 - 这行改掉 换成了 semanticChunks 方法
        List<String> chunks = semanticChunks(text, CHUNK_SIZE);

        // 5. 批量获取embedding并存入PGVector
        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            String embeddingJson = embeddingService.getEmbedding(chunkContent);

            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocumentId(document.getId());
            chunk.setContent(chunkContent);
            chunk.setDocType(docType);
            chunk.setSymptomTag(symptomTag);
            chunk.setChunkIndex(i);

            documentChunkMapper.insertChunkWithEmbedding(chunk, embeddingJson);
        }

        return document.getId();
    }

    @Override
    public boolean deleteDocument(Long id, HttpServletRequest request) {
        userService.getLoginUser(request);
        Document document = this.getById(id);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Document not found");
        }
        // 删除chunks
        QueryWrapper qw = QueryWrapper.create().eq("document_id", id);
        documentChunkMapper.deleteByQuery(qw);
        // 逻辑删除document
        return this.removeById(id);
    }

    @Override
    public Page<DocumentVO> listDocuments(int pageNum, int pageSize) {
        Page<Document> page = this.page(new Page<>(pageNum, pageSize));
        return page.map(doc -> {
            DocumentVO vo = new DocumentVO();
            BeanUtils.copyProperties(doc, vo);
            return vo;
        });
    }

    // 提取文本（支持PDF和TXT）
    private String extractText(MultipartFile file) {
        String filename = file.getOriginalFilename();
        try {
            if (filename != null && filename.endsWith(".pdf")) {
                PDDocument pdDocument = Loader.loadPDF(file.getInputStream().readAllBytes());
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pdDocument);
                pdDocument.close();
                return text;
            } else {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to extract text");
        }
    }

    // Semantic Chunking：按句子边界分块，保证每个chunk语义完整
    private List<String> semanticChunks(String text, int maxChunkSize) {
        String[] sentences = text.split("(?<=[.!?\\n])\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String sentence : sentences) {
            if (current.length() + sentence.length() > maxChunkSize
                    && current.length() > 0) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(sentence).append(" ");
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    // SHA-256 计算
    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed");
        }
    }
}