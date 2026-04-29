package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Location {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private String code;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}