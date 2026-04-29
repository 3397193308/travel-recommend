package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TravelExperienceImage {
    private Long id;
    private Long experienceId;
    private String imageUrl;
    private Integer sort;
    private LocalDateTime createTime;
}
