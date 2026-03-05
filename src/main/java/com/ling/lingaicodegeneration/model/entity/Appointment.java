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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("appointment")
public class Appointment implements Serializable {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;
    private String doctorName;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String status;
    private String note;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Column(value = "is_delete", isLogicDelete = true)
    private Integer isDelete;
}