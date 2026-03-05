package com.ling.lingaicodegeneration.service;

import com.ling.lingaicodegeneration.model.entity.Document;
import com.ling.lingaicodegeneration.model.vo.DocumentVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    // 上传并处理文档（核心ingestion pipeline）
    Long uploadDocument(MultipartFile file, String docType,
                        String symptomTag, HttpServletRequest request);
    // 删除文档
    boolean deleteDocument(Long id, HttpServletRequest request);
    // 文档列表
    Page<DocumentVO> listDocuments(int pageNum, int pageSize);
}