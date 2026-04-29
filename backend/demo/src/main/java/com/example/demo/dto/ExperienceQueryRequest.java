package com.example.demo.dto;

import lombok.Data;

@Data
public class ExperienceQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 10;
    private Long destinationId;
    private Long locationId;
    private String keyword;
    private Integer status;
}
