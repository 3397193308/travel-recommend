package com.example.demo.dto;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {
    private String email;
    private String phone;
    private String avatar;
    private Integer age;
    private Integer gender;
    private Long locationId;
}
