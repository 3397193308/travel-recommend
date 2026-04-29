package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Rating {
    private Long id;
    private Long userId;
    private Long destinationId;
    private Integer score;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
