package com.ling.lingaicodegeneration.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("document_chunk")
public class DocumentChunk implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long documentId;
    private String content;
    private String docType;
    private String symptomTag;
    private Integer chunkIndex;
    private LocalDateTime createTime;
    // embedding 和 content_tsv 不在实体类里，直接用原生SQL写入
}