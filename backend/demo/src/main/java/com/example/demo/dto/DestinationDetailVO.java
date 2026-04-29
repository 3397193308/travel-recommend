package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DestinationDetailVO {
    private Long id;
    private String name;
    private String description;
    private Long locationId;
    private String locationName;
    private String address;
    private String imageUrl;
    private List<String> imageUrls;
    private BigDecimal ticketPrice;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Integer viewCount;
    private Integer collectCount;
    private List<TagVO> tags;
    private List<CategoryVO> categories;
    private Boolean isCollected;
    private Integer userScore;
}
