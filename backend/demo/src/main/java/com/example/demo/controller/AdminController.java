package com.example.demo.controller;

import com.example.demo.dto.AdminLoginResponse;
import com.example.demo.dto.ExperienceAuditRequest;
import com.example.demo.dto.ExperienceQueryRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.Destination;
import com.example.demo.entity.RecommendationConfig;
import com.example.demo.entity.Result;
import com.example.demo.entity.Tag;
import com.example.demo.entity.TravelExperience;
import com.example.demo.service.AdminService;
import com.example.demo.service.RecommendationConfigService;
import com.example.demo.service.TravelExperienceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/admin", "/api/admin"})
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private RecommendationConfigService recommendationConfigService;

    @Autowired
    private TravelExperienceService travelExperienceService;
    
    @Value("${destination.upload-dir:uploads/destination}")
    private String destinationUploadDir;
    
    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return adminService.login(request);
    }

    private Result<String> validateAdmin(String authHeader) {
        String token = authHeader;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!adminService.hasAdminPermission(token)) {
            return Result.error(401, "无管理员权限");
        }
        return null;
    }

    @GetMapping("/dashboard/stats")
    public Result<?> dashboardStats(@RequestHeader("Authorization") String token) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.dashboardStats();
    }

    @GetMapping("/destinations")
    public Result<?> listDestinations(@RequestHeader("Authorization") String token,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.listDestinations(keyword, status, page, pageSize);
    }

    @PostMapping("/upload/destination")
    public Result<?> uploadDestinationImage(@RequestHeader("Authorization") String token,
                                            @RequestParam("file") MultipartFile file) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        
        try {
            Path uploadPath = Paths.get(destinationUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + fileExtension;
            
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String relativeUrl = "/uploads/destination/" + newFilename;
            String fullUrl = baseUrl + relativeUrl;
            
            Map<String, String> data = new HashMap<>();
            data.put("imageUrl", fullUrl);
            data.put("relativeUrl", relativeUrl);
            
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/destinations")
    public Result<?> saveDestination(@RequestHeader("Authorization") String token,
                                     @RequestBody Map<String, Object> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }

        Destination destination = new Destination();
        if (payload.get("id") != null) {
            destination.setId(((Number) payload.get("id")).longValue());
        }
        destination.setName((String) payload.get("name"));
        destination.setDescription((String) payload.get("description"));
        destination.setAddress((String) payload.get("address"));
        destination.setImageUrl((String) payload.get("imageUrl"));
        destination.setImageUrls((String) payload.get("imageUrls"));
        if (payload.get("locationId") != null) {
            destination.setLocationId(((Number) payload.get("locationId")).longValue());
        }
        if (payload.get("ticketPrice") != null && !"".equals(payload.get("ticketPrice"))) {
            destination.setTicketPrice(new java.math.BigDecimal(payload.get("ticketPrice").toString()));
        }
        if (payload.get("status") != null) {
            destination.setStatus(((Number) payload.get("status")).intValue());
        }

        List<Long> tagIds = null;
        if (payload.get("tagIds") instanceof List<?> rawTagIds) {
            tagIds = rawTagIds.stream().map(v -> ((Number) v).longValue()).toList();
        }
        
        List<Long> categoryIds = null;
        if (payload.get("categoryIds") instanceof List<?> rawCategoryIds) {
            categoryIds = rawCategoryIds.stream().map(v -> ((Number) v).longValue()).toList();
        }
        return adminService.saveDestination(destination, tagIds, categoryIds);
    }

    @PatchMapping("/destinations/{id}/status")
    public Result<?> updateDestinationStatus(@RequestHeader("Authorization") String token,
                                             @PathVariable Long id,
                                             @RequestBody Map<String, Integer> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        Integer status = payload.get("status");
        return adminService.updateDestinationStatus(id, status);
    }
    
    @PatchMapping("/destinations/batch-status")
    public Result<?> updateDestinationStatusBatch(@RequestHeader("Authorization") String token,
                                                  @RequestBody Map<String, Object> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        Integer status = ((Number) payload.get("status")).intValue();
        List<?> rawIds = (List<?>) payload.get("ids");
        List<Long> ids = rawIds.stream().map(v -> ((Number) v).longValue()).toList();
        return adminService.updateDestinationStatusBatch(ids, status);
    }

    @DeleteMapping("/destinations/{id}")
    public Result<?> deleteDestination(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.deleteDestination(id);
    }

    @GetMapping("/tags")
    public Result<?> listTags(@RequestHeader("Authorization") String token,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.listTags(keyword, page, pageSize);
    }

    @PostMapping("/tags")
    public Result<?> saveTag(@RequestHeader("Authorization") String token, @RequestBody Tag tag) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.saveTag(tag);
    }

    @PatchMapping("/tags/{id}/status")
    public Result<?> updateTagStatus(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id,
                                     @RequestBody Map<String, Integer> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.updateTagStatus(id, payload.get("status"));
    }
    
    @PatchMapping("/tags/batch-status")
    public Result<?> updateTagStatusBatch(@RequestHeader("Authorization") String token,
                                          @RequestBody Map<String, Object> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        Integer status = ((Number) payload.get("status")).intValue();
        List<?> rawIds = (List<?>) payload.get("ids");
        List<Long> ids = rawIds.stream().map(v -> ((Number) v).longValue()).toList();
        return adminService.updateTagStatusBatch(ids, status);
    }

    @GetMapping("/users")
    public Result<?> listUsers(@RequestHeader("Authorization") String token,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.listUsers(keyword, status, page, pageSize);
    }

    @PatchMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id,
                                      @RequestBody Map<String, Integer> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.updateUserStatus(id, payload.get("status"));
    }
    
    @PatchMapping("/users/batch-status")
    public Result<?> updateUserStatusBatch(@RequestHeader("Authorization") String token,
                                           @RequestBody Map<String, Object> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        Integer status = ((Number) payload.get("status")).intValue();
        List<?> rawIds = (List<?>) payload.get("ids");
        List<Long> ids = rawIds.stream().map(v -> ((Number) v).longValue()).toList();
        return adminService.updateUserStatusBatch(ids, status);
    }

    @GetMapping("/comments")
    public Result<?> listComments(@RequestHeader("Authorization") String token,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Integer status,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.listComments(keyword, status, page, pageSize);
    }

    @PatchMapping("/comments/{id}/status")
    public Result<?> updateCommentStatus(@RequestHeader("Authorization") String token,
                                         @PathVariable Long id,
                                         @RequestBody Map<String, Integer> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return adminService.updateCommentStatus(id, payload.get("status"));
    }
    
    @PatchMapping("/comments/batch-status")
    public Result<?> updateCommentStatusBatch(@RequestHeader("Authorization") String token,
                                              @RequestBody Map<String, Object> payload) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        Integer status = ((Number) payload.get("status")).intValue();
        List<?> rawIds = (List<?>) payload.get("ids");
        List<Long> ids = rawIds.stream().map(v -> ((Number) v).longValue()).toList();
        return adminService.updateCommentStatusBatch(ids, status);
    }
    
    @GetMapping({"/algorithm/config", "/algorithm"})
    public Result<?> getAlgorithmConfig(@RequestHeader("Authorization") String token) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return recommendationConfigService.getConfigResult();
    }
    
    @PutMapping({"/algorithm/config", "/algorithm"})
    public Result<?> updateAlgorithmConfig(@RequestHeader("Authorization") String token,
                                           @RequestBody RecommendationConfig config) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return recommendationConfigService.updateConfig(config);
    }

    @GetMapping("/experiences")
    public Result<?> listExperiences(@RequestHeader("Authorization") String token,
                                     ExperienceQueryRequest request) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        return Result.success(travelExperienceService.adminList(request));
    }

    @GetMapping("/experiences/{id}")
    public Result<?> experienceDetail(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        TravelExperience detail = travelExperienceService.adminDetail(id);
        if (detail == null) {
            return Result.error("分享不存在");
        }
        return Result.success(detail);
    }

    @PatchMapping("/experiences/{id}/audit")
    public Result<?> auditExperience(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id,
                                     @Valid @RequestBody ExperienceAuditRequest request) {
        Result<String> authResult = validateAdmin(token);
        if (authResult != null) {
            return authResult;
        }
        boolean success = travelExperienceService.audit(id, request.getStatus(), request.getRejectReason());
        if (!success) {
            return Result.error("审核失败");
        }
        return Result.success("审核成功");
    }
}
