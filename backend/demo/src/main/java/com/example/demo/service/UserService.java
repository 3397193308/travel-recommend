package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdatePasswordRequest;
import com.example.demo.dto.UpdateUserInfoRequest;
import com.example.demo.entity.Result;
import com.example.demo.entity.User;
import com.example.demo.dto.CategoryVO;
import com.example.demo.dto.TagVO;
import com.example.demo.entity.Category;
import com.example.demo.entity.Destination;
import com.example.demo.entity.Tag;
import com.example.demo.mapper.DestinationMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.TagMapper;
import com.example.demo.mapper.UserCollectionMapper;
import com.example.demo.mapper.UserProfileMapper;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserProfileMapper userProfileMapper;
    
    @Autowired
    private DestinationMapper destinationMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Result<LoginResponse> login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        response.setUserInfo(userInfo);

        return Result.success(response);
    }

    public Result<String> register(RegisterRequest request) {
        String email = StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null;
        String phone = StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null;

        if (userMapper.findByUsername(request.getUsername()) != null) {
            return Result.error("用户名已存在");
        }

        if (email != null && userMapper.findByEmail(email) != null) {
            return Result.error("邮箱已被注册");
        }

        if (phone != null && userMapper.findByPhone(phone) != null) {
            return Result.error("手机号已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);

        int result = userMapper.insert(user);
        if (result > 0) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败");
        }
    }

    public Result<User> getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }
    
    public Result<String> updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        User existing = userMapper.findById(userId);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setAvatar(request.getAvatar());
        existing.setAge(request.getAge());
        existing.setGender(request.getGender());
        existing.setLocationId(request.getLocationId());
        
        int affected = userMapper.update(existing);
        if (affected <= 0) {
            return Result.error("更新用户信息失败");
        }
        return Result.success("更新成功");
    }
    
    public Result<String> updatePassword(Long userId, UpdatePasswordRequest request) {
        User existing = userMapper.findById(userId);
        if (existing == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), existing.getPassword())) {
            return Result.error("原密码错误");
        }
        int affected = userMapper.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
        if (affected <= 0) {
            return Result.error("修改密码失败");
        }
        return Result.success("密码修改成功");
    }
    
    public Result<Map<String, Object>> getProfileOverview(Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("collections", userProfileMapper.countCollections(userId));
        result.put("ratings", userProfileMapper.countRatings(userId));
        result.put("comments", userProfileMapper.countComments(userId));
        return Result.success(result);
    }
    
    public Result<List<Map<String, Object>>> getRecentViews(Long userId, Integer limit) {
        int finalLimit = (limit == null || limit <= 0) ? 10 : limit;
        return Result.success(userProfileMapper.selectRecentViews(userId, finalLimit));
    }
    
    public Result<List<Map<String, Object>>> getMyRatings(Long userId, Integer limit) {
        int finalLimit = (limit == null || limit <= 0) ? 10 : limit;
        return Result.success(userProfileMapper.selectMyRatings(userId, finalLimit));
    }

    public Result<?> getUserPreferences(Long userId) {
        try {
            // 从数据库获取用户的偏好标签
            List<Map<String, Object>> preferences = tagMapper.selectUserPreferences(userId);
            return Result.success(preferences);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取偏好标签失败");
        }
    }

    public Result<?> saveUserPreferences(Long userId, Map<String, Object> preferences) {
        try {
            System.out.println("保存偏好设置开始，userId: " + userId);
            System.out.println("偏好设置数据: " + preferences);
            
            // 获取用户选择的标签ID列表
            List<?> tagIdsRaw = (List<?>) preferences.get("tags");
            System.out.println("标签ID列表(原始): " + tagIdsRaw);
            
            // 获取预算信息
            Integer budgetMin = preferences.get("budgetMin") != null ? ((Number) preferences.get("budgetMin")).intValue() : 0;
            Integer budgetMax = preferences.get("budgetMax") != null ? ((Number) preferences.get("budgetMax")).intValue() : 2000;
            System.out.println("预算范围: " + budgetMin + " - " + budgetMax);
            
            // 获取权重信息
            Map<String, Object> weights = preferences.get("weights") != null ? (Map<String, Object>) preferences.get("weights") : new java.util.HashMap<>();
            System.out.println("权重信息: " + weights);
            
            if (tagIdsRaw != null && !tagIdsRaw.isEmpty()) {
                // 先删除用户现有的偏好标签
                System.out.println("删除用户现有偏好标签");
                int deleteResult = tagMapper.deleteUserPreferences(userId);
                System.out.println("删除结果: " + deleteResult);
                
                // 然后插入新的偏好标签
                System.out.println("插入新的偏好标签");
                for (Object tagIdObj : tagIdsRaw) {
                    Long tagId;
                    if (tagIdObj instanceof Integer) {
                        tagId = ((Integer) tagIdObj).longValue();
                    } else if (tagIdObj instanceof Long) {
                        tagId = (Long) tagIdObj;
                    } else {
                        System.out.println("无效的标签ID类型: " + tagIdObj.getClass());
                        continue;
                    }
                    
                    // 获取标签名称（用于权重查找）
                    String tagName = "";
                    try {
                        com.example.demo.entity.Tag tag = tagMapper.findById(tagId);
                        if (tag != null) {
                            tagName = tag.getName();
                        }
                    } catch (Exception e) {
                        System.out.println("获取标签信息失败: " + e.getMessage());
                    }
                    
                    // 获取权重值
                    Integer weight = 5; // 默认权重
                    if (tagName != null && !tagName.isEmpty() && weights.containsKey(tagName)) {
                        Object weightObj = weights.get(tagName);
                        if (weightObj instanceof Number) {
                            weight = ((Number) weightObj).intValue();
                        }
                    }
                    
                    System.out.println("插入标签ID: " + tagId + ", 权重: " + weight);
                    int insertResult = tagMapper.insertUserPreference(userId, tagId, weight, budgetMin, budgetMax);
                    System.out.println("插入结果: " + insertResult);
                }
            }
            
            System.out.println("保存偏好设置成功");
            return Result.success("偏好设置保存成功");
        } catch (Exception e) {
            System.out.println("保存偏好设置失败");
            e.printStackTrace();
            return Result.error("保存偏好设置失败: " + e.getMessage());
        }
    }

    // 收藏相关方法
    public Result<?> addCollection(Long userId, Long destinationId) {
        try {
            // 检查是否已经收藏
            int count = userCollectionMapper.checkCollection(userId, destinationId);
            if (count > 0) {
                return Result.error("已经收藏过该景点");
            }
            
            // 添加收藏
            int result = userCollectionMapper.addCollection(userId, destinationId);
            if (result > 0) {
                return Result.success("收藏成功");
            } else {
                return Result.error("收藏失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("收藏失败: " + e.getMessage());
        }
    }

    public Result<?> removeCollection(Long userId, Long destinationId) {
        try {
            int result = userCollectionMapper.removeCollection(userId, destinationId);
            if (result > 0) {
                return Result.success("取消收藏成功");
            } else {
                return Result.error("取消收藏失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("取消收藏失败: " + e.getMessage());
        }
    }

    public Result<?> getUserCollections(Long userId) {
        try {
            List<java.util.Map<String, Object>> collections = userCollectionMapper.selectUserCollections(userId);
            
            // 为每个收藏的景点补充标签和分类信息
            List<java.util.Map<String, Object>> result = new ArrayList<>();
            for (java.util.Map<String, Object> item : collections) {
                Long destId = Long.valueOf(item.get("id").toString());
                
                // 获取标签信息
                List<Tag> tags = tagMapper.selectByDestinationId(destId);
                List<TagVO> tagVOs = tags.stream().map(tag -> {
                    TagVO vo = new TagVO();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    return vo;
                }).collect(java.util.stream.Collectors.toList());
                item.put("tags", tagVOs);
                
                // 获取分类信息
                List<Long> categoryIds = destinationMapper.selectCategoryIdsByDestinationId(destId);
                List<CategoryVO> categories = new ArrayList<>();
                for (Long categoryId : categoryIds) {
                    Category category = destinationMapper.selectCategoryById(categoryId);
                    if (category != null) {
                        CategoryVO vo = new CategoryVO();
                        vo.setId(category.getId());
                        vo.setName(category.getName());
                        vo.setParentId(category.getParentId());
                        vo.setSort(category.getSort());
                        categories.add(vo);
                    }
                }
                item.put("categories", categories);
                
                result.add(item);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    public Result<?> checkCollection(Long userId, Long destinationId) {
        try {
            int count = userCollectionMapper.checkCollection(userId, destinationId);
            return Result.success(count > 0);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("检查收藏状态失败: " + e.getMessage());
        }
    }

    @Autowired
    private UserCollectionMapper userCollectionMapper;
}
