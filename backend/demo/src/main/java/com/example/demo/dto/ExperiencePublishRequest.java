package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ExperiencePublishRequest {
    @NotNull(message = "景点ID不能为空")
    private Long destinationId;

    @NotBlank(message = "体验标题不能为空")
    @Size(min = 1, max = 20, message = "体验标题长度需在1-20字之间")
    private String title;

    @NotBlank(message = "体验正文不能为空")
    @Size(min = 10, max = 2000, message = "体验正文长度需在10-2000字之间")
    private String content;

    @NotNull(message = "星级评分不能为空")
    @Min(value = 1, message = "星级评分最低为1星")
    @Max(value = 5, message = "星级评分最高为5星")
    private Integer star;

    private List<String> imageUrls;
}
