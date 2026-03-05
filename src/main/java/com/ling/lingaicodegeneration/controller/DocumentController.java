package com.ling.lingaicodegeneration.controller;

import com.ling.lingaicodegeneration.annotation.AuthCheck;
import com.ling.lingaicodegeneration.common.BaseResponse;
import com.ling.lingaicodegeneration.common.ResultUtils;
import com.ling.lingaicodegeneration.constant.UserConstant;
import com.ling.lingaicodegeneration.model.vo.DocumentVO;
import com.ling.lingaicodegeneration.service.DocumentService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/document")
public class DocumentController {

    @Resource
    private DocumentService documentService;

    // 上传文档（仅admin）
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") String docType,
            @RequestParam(value = "symptomTag", required = false) String symptomTag,
            HttpServletRequest request) {
        Long id = documentService.uploadDocument(file, docType, symptomTag, request);
        return ResultUtils.success(id);
    }

    // 删除文档（仅admin）
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteDocument(
            @RequestParam Long id,
            HttpServletRequest request) {
        return ResultUtils.success(documentService.deleteDocument(id, request));
    }

    // 文档列表（所有登录用户可查）
    @GetMapping("/list")
    public BaseResponse<Page<DocumentVO>> listDocuments(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResultUtils.success(documentService.listDocuments(pageNum, pageSize));
    }
}