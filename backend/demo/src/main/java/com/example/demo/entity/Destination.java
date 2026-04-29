package com.example.demo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Destination {
    private Long id;
    private String name;
    private String description;
    private Long locationId;
    private String address;
    private String imageUrl;
    private String imageUrls;
    private BigDecimal ticketPrice;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Integer viewCount;
    private Integer collectCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
