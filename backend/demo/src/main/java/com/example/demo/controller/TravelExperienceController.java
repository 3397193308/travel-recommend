package com.example.demo.controller;

import com.example.demo.dto.ExperiencePublishRequest;
import com.example.demo.dto.ExperienceQueryRequest;
import com.example.demo.entity.Result;
import com.example.demo.entity.TravelExperience;
import com.example.demo.service.TravelExperienceService;
import com.example.demo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping({"/experience", "/api/experience"})
@CrossOrigin(origins = "*")
public class TravelExperienceController {

    @Autowired
    private TravelExperienceService travelExperienceService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${experience.upload-dir:uploads/experience}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    private Long parseRequiredUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private Long parseOptionalUserId(String token) {
        if (token == null) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @PostMapping
    public Result<Map<String, Object>> publish(@RequestHeader("Authorization") String token,
                                               @Valid @RequestBody ExperiencePublishRequest request) {
        Long userId = parseRequiredUserId(token);
        return Result.success(travelExperienceService.publish(userId, request));
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(ExperienceQueryRequest request) {
        return Result.success(travelExperienceService.listApproved(request));
    }

    @GetMapping("/{id}")
    public Result<TravelExperience> detail(@PathVariable Long id,
                                           @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = parseOptionalUserId(token);
        TravelExperience detail = travelExperienceService.detail(id, userId);
        if (detail == null) {
            return Result.error("旅游体验不存在或不可查看");
        }
        return Result.success(detail);
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> my(@RequestHeader("Authorization") String token,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = parseRequiredUserId(token);
        return Result.success(travelExperienceService.myExperiences(userId, status, page, pageSize));
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadImage(@RequestHeader("Authorization") String token,
                                                   @RequestParam("file") MultipartFile file) {
        parseRequiredUserId(token);
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalName);
        if (extension == null) {
            return Result.error("图片格式不正确");
        }
        extension = extension.toLowerCase();
        Set<String> allowedExt = Set.of("jpg", "jpeg", "png", "webp");
        if (!allowedExt.contains(extension)) {
            return Result.error("仅支持 jpg/jpeg/png/webp 格式");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "." + extension;
            Path targetFile = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            String relativeUrl = "/uploads/experience/" + fileName;
            String fullUrl = baseUrl + relativeUrl;
            Map<String, String> data = new HashMap<>();
            data.put("imageUrl", fullUrl);
            data.put("relativeUrl", relativeUrl);
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
