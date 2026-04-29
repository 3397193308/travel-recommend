package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull(message = "景点ID不能为空")
    private Long destinationId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private Long parentId;
}
