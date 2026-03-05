package com.ling.lingaicodegeneration.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentVO {
    private Long id;
    private String title;
    private String docType;
    private Long uploadUserId;
    private LocalDateTime createTime;
}