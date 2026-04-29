package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExperienceAuditRequest {
    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @Size(max = 255, message = "驳回原因不能超过255字")
    private String rejectReason;
}
