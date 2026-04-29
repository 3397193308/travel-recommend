package com.example.demo.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TravelExperience {
    private Long id;
    private Long userId;
    private Long destinationId;
    private String title;
    private String content;
    private Integer star;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String username;
    private String destinationName;
    private String provinceName;
    private String cityName;
    private List<TravelExperienceImage> images;
}
