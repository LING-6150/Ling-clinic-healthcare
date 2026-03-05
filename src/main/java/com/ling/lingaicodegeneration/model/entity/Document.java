package com.ling.lingaicodegeneration.model.entity;

import com.mybatisflex.annotation.Column;
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
@Table("document")
public class Document implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String title;
    private String docType;
    private String fileHash;
    private Long uploadUserId;
    private LocalDateTime createTime;

    @Column(value = "is_delete", isLogicDelete = true)
    private Integer isDelete;
}