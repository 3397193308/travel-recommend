package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DestinationQueryRequest {
    private Integer page = 1;
    private Integer pageSize = 12;
    private String keyword;
    private Long locationId;
    private Long tagId;
    private Long categoryId;
    private String sortBy = "view_count";
    private String sortOrder = "desc";
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private BigDecimal maxRating;
}
