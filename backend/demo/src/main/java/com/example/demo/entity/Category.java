package com.example.demo.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Category {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
