package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdatePasswordRequest;
import com.example.demo.dto.UpdateUserInfoRequest;
import com.example.demo.entity.Result;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/user", "/api/user"})
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${avatar.upload-dir:uploads/avatar}")
    private String avatarUploadDir;
    
    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    private Long parseUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        Long userId = parseUserId(token);
        return userService.getUserInfo(userId);
    }
    
    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestHeader("Authorization") String token,
                                         @RequestBody UpdateUserInfoRequest request) {
        Long userId = parseUserId(token);
        return userService.updateUserInfo(userId, request);
    }
    
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestHeader("Authorization") String token,
                                         @Valid @RequestBody UpdatePasswordRequest request) {
        Long userId = parseUserId(token);
        return userService.updatePassword(userId, request);
    }
    
    @GetMapping("/profile/overview")
    public Result<?> getProfileOverview(@RequestHeader("Authorization") String token) {
        Long userId = parseUserId(token);
        return userService.getProfileOverview(userId);
    }
    
    @GetMapping("/profile/recent-views")
    public Result<?> getRecentViews(@RequestHeader("Authorization") String token,
                                    @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = parseUserId(token);
        return userService.getRecentViews(userId, limit);
    }
    
    @GetMapping("/profile/ratings")
    public Result<?> getMyRatings(@RequestHeader("Authorization") String token,
                                  @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = parseUserId(token);
        return userService.getMyRatings(userId, limit);
    }

    @GetMapping("/preferences")
    public Result<?> getPreferences(@RequestHeader("Authorization") String token) {
        Long userId = parseUserId(token);
        return userService.getUserPreferences(userId);
    }

    @PostMapping("/preferences")
    public Result<?> savePreferences(@RequestHeader("Authorization") String token, @RequestBody java.util.Map<String, Object> preferences) {
        Long userId = parseUserId(token);
        return userService.saveUserPreferences(userId, preferences);
    }

    // 收藏相关接口
    @PostMapping("/collections")
    public Result<?> addCollection(@RequestHeader("Authorization") String token, @RequestBody java.util.Map<String, Object> data) {
        Long userId = parseUserId(token);
        Long destinationId = ((Number) data.get("destinationId")).longValue();
        return userService.addCollection(userId, destinationId);
    }

    @DeleteMapping("/collections/{destinationId}")
    public Result<?> removeCollection(@RequestHeader("Authorization") String token, @PathVariable Long destinationId) {
        Long userId = parseUserId(token);
        return userService.removeCollection(userId, destinationId);
    }

    @GetMapping("/collections")
    public Result<?> getUserCollections(@RequestHeader("Authorization") String token) {
        Long userId = parseUserId(token);
        return userService.getUserCollections(userId);
    }

    @GetMapping("/collections/check/{destinationId}")
    public Result<?> checkCollection(@RequestHeader("Authorization") String token, @PathVariable Long destinationId) {
        Long userId = parseUserId(token);
        return userService.checkCollection(userId, destinationId);
    }
    
    @PostMapping("/upload/avatar")
    public Result<?> uploadAvatar(@RequestHeader("Authorization") String token,
                                  @RequestParam("file") MultipartFile file) {
        Long userId = parseUserId(token);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        try {
            Path uploadPath = Paths.get(avatarUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + fileExtension;
            
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String relativeUrl = "/uploads/avatar/" + newFilename;
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
