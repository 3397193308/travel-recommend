package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long userId;
    private Long destinationId;
    private String content;
    private Long parentId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private String username;
    private String userAvatar;
}
