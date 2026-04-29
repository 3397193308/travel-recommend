package com.example.demo.service;

import com.example.demo.dto.AdminLoginResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Destination;
import com.example.demo.entity.Result;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Result<AdminLoginResponse> login(LoginRequest request) {
        Admin admin = adminMapper.findAdminByUsername(request.getUsername());
        if (admin == null) {
            return Result.error("管理员账号或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            return Result.error("管理员账号已禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            return Result.error("管理员账号或密码错误");
        }

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole());

        AdminLoginResponse.AdminInfo adminInfo = new AdminLoginResponse.AdminInfo();
        adminInfo.setId(admin.getId());
        adminInfo.setUsername(admin.getUsername());
        adminInfo.setRealName(admin.getRealName());
        adminInfo.setRole(admin.getRole());

        AdminLoginResponse response = new AdminLoginResponse();
        response.setToken(token);
        response.setAdminInfo(adminInfo);
        return Result.success(response);
    }

    public boolean hasAdminPermission(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        String role = jwtUtil.getRoleFromToken(token);
        if (!"admin".equals(role) && !"super_admin".equals(role)) {
            return false;
        }
        Long adminId = jwtUtil.getUserIdFromToken(token);
        Admin admin = adminMapper.findAdminById(adminId);
        return admin != null && admin.getStatus() != null && admin.getStatus() == 1;
    }

    public Result<Map<String, Object>> dashboardStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", adminMapper.countUsers());
        data.put("destinationCount", adminMapper.countDestinations());
        data.put("commentCount", adminMapper.countComments());
        data.put("ratingCount", adminMapper.countRatings());
        data.put("recentUsers", adminMapper.listRecentUsers());
        data.put("recentComments", adminMapper.listRecentComments());
        return Result.success(data);
    }

    public Result<Map<String, Object>> listDestinations(String keyword, Integer status, Integer page, Integer pageSize) {
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (pageNum - 1) * size;
        List<Map<String, Object>> list = adminMapper.listDestinationsForAdmin(keyword, status, offset, size);
        Long total = adminMapper.countDestinationsForAdmin(keyword, status);

        for (Map<String, Object> item : list) {
            Long destinationId = ((Number) item.get("id")).longValue();
            item.put("tagIds", adminMapper.listDestinationTagIds(destinationId));
            item.put("categoryIds", adminMapper.listDestinationCategoryIds(destinationId));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", pageNum);
        data.put("pageSize", size);
        return Result.success(data);
    }

    public Result<String> saveDestination(Destination destination, List<Long> tagIds) {
        return saveDestination(destination, tagIds, null);
    }
    
    public Result<String> saveDestination(Destination destination, List<Long> tagIds, List<Long> categoryIds) {
        if (destination.getStatus() == null) {
            destination.setStatus(1);
        }
        int affected;
        if (destination.getId() == null) {
            affected = adminMapper.insertDestination(destination);
        } else {
            affected = adminMapper.updateDestination(destination);
        }
        if (affected <= 0) {
            return Result.error("保存景点失败");
        }

        Long destinationId = destination.getId();
        adminMapper.deleteDestinationTags(destinationId);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                adminMapper.insertDestinationTag(destinationId, tagId);
            }
        }
        
        adminMapper.deleteDestinationCategories(destinationId);
        if (categoryIds != null) {
            for (Long categoryId : categoryIds) {
                adminMapper.insertDestinationCategory(destinationId, categoryId);
            }
        }
        return Result.success("保存成功");
    }

    public Result<String> updateDestinationStatus(Long id, Integer status) {
        int affected = adminMapper.updateDestinationStatus(id, status);
        if (affected <= 0) {
            return Result.error("更新失败");
        }
        return Result.success("更新成功");
    }
    
    public Result<String> updateDestinationStatusBatch(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择至少一条数据");
        }
        int affected = adminMapper.updateDestinationStatusBatch(ids, status);
        if (affected <= 0) {
            return Result.error("批量更新失败");
        }
        return Result.success("批量更新成功");
    }

    public Result<String> deleteDestination(Long id) {
        adminMapper.deleteDestinationTags(id);
        int affected = adminMapper.deleteDestination(id);
        if (affected <= 0) {
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }

    public Result<Map<String, Object>> listTags(String keyword, Integer page, Integer pageSize) {
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (pageNum - 1) * size;
        List<Tag> list = adminMapper.listTags(keyword, offset, size);
        Long total = adminMapper.countTags(keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", pageNum);
        data.put("pageSize", size);
        return Result.success(data);
    }

    public Result<String> saveTag(Tag tag) {
        if (tag.getStatus() == null) {
            tag.setStatus(1);
        }
        if (tag.getSortOrder() == null) {
            tag.setSortOrder(0);
        }
        int affected = tag.getId() == null ? adminMapper.insertTag(tag) : adminMapper.updateTag(tag);
        if (affected <= 0) {
            return Result.error("保存标签失败");
        }
        return Result.success("保存成功");
    }

    public Result<String> updateTagStatus(Long id, Integer status) {
        int affected = adminMapper.updateTagStatus(id, status);
        if (affected <= 0) {
            return Result.error("更新失败");
        }
        return Result.success("更新成功");
    }
    
    public Result<String> updateTagStatusBatch(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择至少一条数据");
        }
        int affected = adminMapper.updateTagStatusBatch(ids, status);
        if (affected <= 0) {
            return Result.error("批量更新失败");
        }
        return Result.success("批量更新成功");
    }

    public Result<Map<String, Object>> listUsers(String keyword, Integer status, Integer page, Integer pageSize) {
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (pageNum - 1) * size;
        List<User> list = adminMapper.listUsers(keyword, status, offset, size);
        Long total = adminMapper.countUsersForAdmin(keyword, status);

        for (User user : list) {
            user.setPassword(null);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", pageNum);
        data.put("pageSize", size);
        return Result.success(data);
    }

    public Result<String> updateUserStatus(Long id, Integer status) {
        int affected = adminMapper.updateUserStatus(id, status);
        if (affected <= 0) {
            return Result.error("更新失败");
        }
        return Result.success("更新成功");
    }
    
    public Result<String> updateUserStatusBatch(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择至少一条数据");
        }
        int affected = adminMapper.updateUserStatusBatch(ids, status);
        if (affected <= 0) {
            return Result.error("批量更新失败");
        }
        return Result.success("批量更新成功");
    }

    public Result<Map<String, Object>> listComments(String keyword, Integer status, Integer page, Integer pageSize) {
        int pageNum = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (pageNum - 1) * size;
        List<Map<String, Object>> list = adminMapper.listComments(keyword, status, offset, size);
        Long total = adminMapper.countCommentsForAdmin(keyword, status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", pageNum);
        data.put("pageSize", size);
        return Result.success(data);
    }

    public Result<String> updateCommentStatus(Long id, Integer status) {
        int affected = adminMapper.updateCommentStatus(id, status);
        if (affected <= 0) {
            return Result.error("更新失败");
        }
        return Result.success("更新成功");
    }
    
    public Result<String> updateCommentStatusBatch(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择至少一条数据");
        }
        int affected = adminMapper.updateCommentStatusBatch(ids, status);
        if (affected <= 0) {
            return Result.error("批量更新失败");
        }
        return Result.success("批量更新成功");
    }
}
