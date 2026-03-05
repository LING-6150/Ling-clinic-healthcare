package com.ling.lingaicodegeneration.model.dto.appointment;

import com.ling.lingaicodegeneration.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppointmentQueryRequest extends PageRequest implements Serializable {
    private Long userId;
    private String status;
    private String doctorName;
}