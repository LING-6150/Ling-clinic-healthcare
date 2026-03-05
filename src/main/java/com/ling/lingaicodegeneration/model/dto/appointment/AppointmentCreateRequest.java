package com.ling.lingaicodegeneration.model.dto.appointment;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class AppointmentCreateRequest implements Serializable {
    private String doctorName;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String note;
}