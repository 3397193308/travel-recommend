package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Location;
import com.example.demo.entity.Result;
import com.example.demo.service.DestinationService;
import com.example.demo.service.LocationService;
import com.example.demo.service.TagService;
import com.example.demo.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/destination")
@CrossOrigin(origins = "*")
public class DestinationController {

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private TagService tagService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long parseUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        }
        return null;
    }

    private Long parseUserIdRequired(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(
            DestinationQueryRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = parseUserId(token);
        Map<String, Object> result = destinationService.getDestinationList(request, userId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DestinationDetailVO> getDetail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = parseUserId(token);
        DestinationDetailVO detail = destinationService.getDestinationDetail(id, userId);
        if (detail == null) {
            return Result.error("景点不存在");
        }
        return Result.success(detail);
    }

    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHot(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> list = destinationService.getHotDestinations(limit);
        return Result.success(list);
    }

    @PostMapping("/{id}/collect")
    public Result<String> collect(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = parseUserIdRequired(token);
        boolean success = destinationService.collectDestination(userId, id);
        if (success) {
            return Result.success("收藏成功");
        }
        return Result.error("收藏失败");
    }

    @DeleteMapping("/{id}/collect")
    public Result<String> uncollect(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        Long userId = parseUserIdRequired(token);
        boolean success = destinationService.uncollectDestination(userId, id);
        if (success) {
            return Result.success("取消收藏成功");
        }
        return Result.error("取消收藏失败");
    }

    @PostMapping("/rate")
    public Result<String> rate(
            @Valid @RequestBody RateRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = parseUserIdRequired(token);
        boolean success = destinationService.rateDestination(userId, request.getDestinationId(), request.getScore());
        if (success) {
            return Result.success("评分成功");
        }
        return Result.error("评分失败");
    }

    @GetMapping("/{id}/comments")
    public Result<Map<String, Object>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = destinationService.getComments(id, page, pageSize);
        return Result.success(result);
    }

    @PostMapping("/comment")
    public Result<Comment> addComment(
            @Valid @RequestBody CommentRequest request,
            @RequestHeader("Authorization") String token) {
        Long userId = parseUserIdRequired(token);
        Comment comment = destinationService.addComment(userId, request.getDestinationId(), request.getContent(), request.getParentId());
        return Result.success(comment);
    }

    @GetMapping("/locations/level/{level}")
    public Result<List<Location>> getLocationsByLevel(@PathVariable Integer level) {
        List<Location> locations = locationService.getLocationsByLevel(level);
        return Result.success(locations);
    }

    @GetMapping("/locations/parent/{parentId}")
    public Result<List<Location>> getLocationsByParentId(@PathVariable Long parentId) {
        List<Location> locations = locationService.getLocationsByParentId(parentId);
        return Result.success(locations);
    }

    @GetMapping("/recommend")
    public Result<List<Map<String, Object>>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = parseUserId(token);
        List<Map<String, Object>> recommendations = destinationService.getRecommendedDestinations(userId, limit);
        return Result.success(recommendations);
    }

    @GetMapping("/recommend/for-you")
    public Result<List<Map<String, Object>>> getRecommendedForYou(
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = parseUserId(token);
        List<Map<String, Object>> recommendations = destinationService.getRecommendedForYou(userId, limit);
        return Result.success(recommendations);
    }
}
