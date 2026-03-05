package com.ling.lingaicodegeneration.model.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentVO {
    private Long id;
    private Long userId;
    private String doctorName;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String status;
    private String note;
    private LocalDateTime createTime;
}