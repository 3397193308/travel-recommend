package com.example.demo.entity;

import lombok.Data;

@Data
public class Admin {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String role;
    private Integer status;
}
