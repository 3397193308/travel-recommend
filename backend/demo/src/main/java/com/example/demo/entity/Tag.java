package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Tag {
    private Long id;
    private String name;
    private String type;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
