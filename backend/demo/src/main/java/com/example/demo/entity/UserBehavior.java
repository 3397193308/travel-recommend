package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserBehavior {
    private Long id;
    private Long userId;
    private Long destinationId;
    private String behaviorType;
    private LocalDateTime createTime;
}
