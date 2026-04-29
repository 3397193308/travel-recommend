package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.mapper.*;
import com.example.demo.util.SimilarityCalculator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DestinationService {

    @Autowired
    private DestinationMapper destinationMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private RatingMapper ratingMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserCollectionMapper userCollectionMapper;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RecommendationConfigService recommendationConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getDestinationList(DestinationQueryRequest request, Long userId) {
        int offset = (request.getPage() - 1) * request.getPageSize();
        List<Destination> destinations = destinationMapper.selectList(request, offset, request.getPageSize());
        int total = destinationMapper.count(request);

        List<Long> collectedIds = new ArrayList<>();
        if (userId != null) {
            collectedIds = userCollectionMapper.selectCollectionDestinationIds(userId);
        }
        final List<Long> finalCollectedIds = collectedIds;

        List<Map<String, Object>> list = destinations.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("name", d.getName());
            map.put("description", d.getDescription());
            map.put("locationId", d.getLocationId());
            
            // 获取地点名称
            if (d.getLocationId() != null) {
                Location location = locationMapper.findById(d.getLocationId());
                if (location != null) {
                    map.put("locationName", location.getName());
                    map.put("city", location.getName());
                    // 获取省份信息
                    if (location.getParentId() != null) {
                        Location province = locationMapper.findById(location.getParentId());
                        if (province != null) {
                            map.put("province", province.getName());
                        }
                    }
                }
            } else {
                map.put("province", "");
                map.put("city", "");
            }
            
            map.put("imageUrl", d.getImageUrl());
            map.put("ticketPrice", d.getTicketPrice());
            map.put("averageRating", d.getAverageRating());
            map.put("ratingCount", d.getRatingCount());
            map.put("viewCount", d.getViewCount());
            map.put("collectCount", d.getCollectCount());
            map.put("isCollected", finalCollectedIds.contains(d.getId()));
            
            List<Tag> tags = tagMapper.selectByDestinationId(d.getId());
            map.put("tags", tags.stream().map(this::convertToTagVO).collect(Collectors.toList()));
            
            // 获取分类信息
            List<Long> categoryIds = destinationMapper.selectCategoryIdsByDestinationId(d.getId());
            List<CategoryVO> categories = new ArrayList<>();
            for (Long categoryId : categoryIds) {
                Category category = destinationMapper.selectCategoryById(categoryId);
                if (category != null) {
                    categories.add(convertToCategoryVO(category));
                }
            }
            map.put("categories", categories);
            
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", request.getPage());
        result.put("pageSize", request.getPageSize());
        
        return result;
    }

    @Autowired
    private LocationMapper locationMapper;

    public DestinationDetailVO getDestinationDetail(Long id, Long userId) {
        Destination destination = destinationMapper.findById(id);
        if (destination == null) {
            return null;
        }

        destinationMapper.incrementViewCount(id);

        if (userId != null) {
            UserBehavior viewBehavior = new UserBehavior();
            viewBehavior.setUserId(userId);
            viewBehavior.setDestinationId(id);
            viewBehavior.setBehaviorType("view");
            userBehaviorMapper.insert(viewBehavior);
        }

        DestinationDetailVO vo = new DestinationDetailVO();
        vo.setId(destination.getId());
        vo.setName(destination.getName());
        vo.setDescription(destination.getDescription());
        vo.setLocationId(destination.getLocationId());
        
        // 获取地点名称
        if (destination.getLocationId() != null) {
            Location location = locationMapper.findById(destination.getLocationId());
            if (location != null) {
                vo.setLocationName(location.getName());
            }
        }
        
        vo.setAddress(destination.getAddress());
        vo.setImageUrl(destination.getImageUrl());
        vo.setTicketPrice(destination.getTicketPrice());
        vo.setAverageRating(destination.getAverageRating());
        vo.setRatingCount(destination.getRatingCount());
        vo.setViewCount(destination.getViewCount());
        vo.setCollectCount(destination.getCollectCount());

        if (destination.getImageUrls() != null && !destination.getImageUrls().isEmpty()) {
            try {
                List<String> urls = objectMapper.readValue(destination.getImageUrls(), new TypeReference<List<String>>() {});
                vo.setImageUrls(urls);
            } catch (Exception e) {
                vo.setImageUrls(new ArrayList<>());
            }
        } else {
            vo.setImageUrls(new ArrayList<>());
        }

        List<Tag> tags = tagMapper.selectByDestinationId(id);
        vo.setTags(tags.stream().map(this::convertToTagVO).collect(Collectors.toList()));

        // 获取景点分类
        List<Long> categoryIds = destinationMapper.selectCategoryIdsByDestinationId(id);
        List<CategoryVO> categories = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            Category category = destinationMapper.selectCategoryById(categoryId);
            if (category != null) {
                categories.add(convertToCategoryVO(category));
            }
        }
        vo.setCategories(categories);

        if (userId != null) {
            int collectedCount = userCollectionMapper.checkCollection(userId, id);
            vo.setIsCollected(collectedCount > 0);

            Rating rating = ratingMapper.findByUserAndDestination(userId, id);
            vo.setUserScore(rating != null ? rating.getScore() : null);
        } else {
            vo.setIsCollected(false);
            vo.setUserScore(null);
        }

        return vo;
    }

    public List<Map<String, Object>> getHotDestinations(int limit) {
        List<Destination> destinations = destinationMapper.selectHotDestinations(limit);
        return destinations.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("name", d.getName());
            map.put("locationId", d.getLocationId());
            
            // 获取地点名称
            if (d.getLocationId() != null) {
                Location location = locationMapper.findById(d.getLocationId());
                if (location != null) {
                    map.put("locationName", location.getName());
                    map.put("city", location.getName());
                    // 获取省份信息
                    if (location.getParentId() != null) {
                        Location province = locationMapper.findById(location.getParentId());
                        if (province != null) {
                            map.put("province", province.getName());
                        }
                    }
                }
            } else {
                map.put("province", "");
                map.put("city", "");
            }
            
            map.put("imageUrl", d.getImageUrl());
            map.put("averageRating", d.getAverageRating());
            map.put("viewCount", d.getViewCount());
            
            // 获取标签信息
            List<Tag> tags = tagMapper.selectByDestinationId(d.getId());
            map.put("tags", tags.stream().map(this::convertToTagVO).collect(Collectors.toList()));
            
            // 获取分类信息
            List<Long> categoryIds = destinationMapper.selectCategoryIdsByDestinationId(d.getId());
            List<CategoryVO> categories = new ArrayList<>();
            for (Long categoryId : categoryIds) {
                Category category = destinationMapper.selectCategoryById(categoryId);
                if (category != null) {
                    categories.add(convertToCategoryVO(category));
                }
            }
            map.put("categories", categories);
            
            return map;
        }).collect(Collectors.toList());
    }

    public boolean collectDestination(Long userId, Long destinationId) {
        Destination destination = destinationMapper.findById(destinationId);
        if (destination == null) {
            return false;
        }

        int existingCount = userCollectionMapper.checkCollection(userId, destinationId);
        if (existingCount > 0) {
            return true;
        }

        int result = userCollectionMapper.addCollection(userId, destinationId);
        if (result > 0) {
            destinationMapper.incrementCollectCount(destinationId);
            
            UserBehavior collectBehavior = new UserBehavior();
            collectBehavior.setUserId(userId);
            collectBehavior.setDestinationId(destinationId);
            collectBehavior.setBehaviorType("collect");
            userBehaviorMapper.insert(collectBehavior);
            
            return true;
        }
        return false;
    }

    public boolean uncollectDestination(Long userId, Long destinationId) {
        int deleted = userCollectionMapper.removeCollection(userId, destinationId);
        if (deleted > 0) {
            destinationMapper.decrementCollectCount(destinationId);
            return true;
        }
        return false;
    }

    public boolean rateDestination(Long userId, Long destinationId, Integer score) {
        Destination destination = destinationMapper.findById(destinationId);
        if (destination == null) {
            return false;
        }

        Rating existing = ratingMapper.findByUserAndDestination(userId, destinationId);
        if (existing != null) {
            ratingMapper.updateScore(userId, destinationId, score);
        } else {
            Rating rating = new Rating();
            rating.setUserId(userId);
            rating.setDestinationId(destinationId);
            rating.setScore(score);
            ratingMapper.insert(rating);
        }

        Double avgRating = ratingMapper.getAverageScore(destinationId);
        int count = ratingMapper.countByDestination(destinationId);
        ratingMapper.updateDestinationRating(destinationId, avgRating, count);

        return true;
    }

    public Map<String, Object> getComments(Long destinationId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Comment> comments = commentMapper.selectByDestinationId(destinationId, offset, pageSize);
        int total = commentMapper.countByDestinationId(destinationId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", comments);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return result;
    }

    public Comment addComment(Long userId, Long destinationId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setDestinationId(destinationId);
        comment.setContent(content);
        comment.setParentId(parentId);
        
        commentMapper.insert(comment);
        return commentMapper.findById(comment.getId());
    }

    private TagVO convertToTagVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setType(tag.getType());
        vo.setDescription(tag.getDescription());
        return vo;
    }

    private CategoryVO convertToCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setSort(category.getSort());
        return vo;
    }

    // ========================================// 推荐功能实现// ========================================

    public List<Map<String, Object>> getRecommendedDestinations(Long userId, int limit) {
        if (userId == null) {
            return getHotDestinations(limit);
        }
        RecommendationConfig config = recommendationConfigService.getCurrentConfig();

        // 获取用户偏好标签
        List<Long> userPreferences = tagMapper.selectPreferenceTagIdsByUserId(userId);
        
        List<Map<String, Object>> recommendations = new ArrayList<>();

        // 1. 基于内容的推荐（景点相似度）- 优先级最高
        int contentLimit = calcWeightedLimit(limit, config.getRecommendationContentWeight());
        int collaborativeLimit = calcWeightedLimit(limit, config.getRecommendationCollaborativeWeight());
        int preferenceLimit = calcWeightedLimit(limit, config.getRecommendationPreferenceWeight());
        int hotLimit = Math.max(limit - contentLimit - collaborativeLimit - preferenceLimit, 0);
        
        List<Map<String, Object>> contentRecommendations = getContentBasedRecommendations(userId, contentLimit, config);
        recommendations.addAll(contentRecommendations);

        // 2. 基于协同过滤的推荐（用户相似度）
        List<Map<String, Object>> collaborativeRecommendations = getCollaborativeFilteringRecommendations(userId, userPreferences, collaborativeLimit, config);
        recommendations.addAll(collaborativeRecommendations);

        // 3. 基于用户偏好的推荐
        List<Map<String, Object>> preferenceRecommendations = getPreferenceBasedRecommendations(userId, userPreferences, preferenceLimit);
        recommendations.addAll(preferenceRecommendations);

        // 4. 如果推荐数量不足，补充热门景点
        if (recommendations.size() < limit || hotLimit > 0) {
            int finalHotLimit = Math.max(Math.max(limit - recommendations.size(), 0), hotLimit);
            List<Map<String, Object>> hotDestinations = getHotDestinations(finalHotLimit);
            // 过滤掉已经推荐的景点
            Set<Long> recommendedIds = recommendations.stream()
                .map(m -> (Long) m.get("id"))
                .collect(Collectors.toSet());
            hotDestinations.stream()
                .filter(m -> !recommendedIds.contains(m.get("id")))
                .forEach(recommendations::add);
        }

        // 限制返回数量
        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getPreferenceBasedRecommendations(Long userId, List<Long> userPreferences, int limit) {
        if (userPreferences == null || userPreferences.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取具有这些标签的景点
        List<Destination> destinations = destinationMapper.selectByTagIds(userPreferences, limit);
        return destinations.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("name", d.getName());
            map.put("locationId", d.getLocationId());
            
            // 获取地点名称
            if (d.getLocationId() != null) {
                Location location = locationMapper.findById(d.getLocationId());
                if (location != null) {
                    map.put("locationName", location.getName());
                    map.put("city", location.getName());
                    // 获取省份信息
                    if (location.getParentId() != null) {
                        Location province = locationMapper.findById(location.getParentId());
                        if (province != null) {
                            map.put("province", province.getName());
                        }
                    }
                }
            } else {
                map.put("province", "");
                map.put("city", "");
            }
            
            map.put("imageUrl", d.getImageUrl());
            map.put("ticketPrice", d.getTicketPrice());
            map.put("averageRating", d.getAverageRating());
            map.put("reason", "基于您的偏好推荐");
            map.put("matchRate", calculateMatchRate(d, userPreferences, 0));
            
            // 添加标签信息
            List<Tag> tags = tagMapper.selectByDestinationId(d.getId());
            map.put("tags", tags.stream().map(this::convertToTagVO).collect(Collectors.toList()));
            
            // 获取分类信息
            List<Long> categoryIds = destinationMapper.selectCategoryIdsByDestinationId(d.getId());
            List<CategoryVO> categories = new ArrayList<>();
            for (Long categoryId : categoryIds) {
                Category category = destinationMapper.selectCategoryById(categoryId);
                if (category != null) {
                    categories.add(convertToCategoryVO(category));
                }
            }
            map.put("categories", categories);
            
            return map;
        }).collect(Collectors.toList());
    }

    private int calcWeightedLimit(int total, Double weight) {
        if (total <= 0 || weight == null || weight <= 0) {
            return 0;
        }
        return Math.max((int) Math.round(total * weight), 0);
    }
    
    private List<Map<String, Object>> getCollaborativeFilteringRecommendations(Long userId, List<Long> userPreferences, int limit, RecommendationConfig config) {
        if (limit <= 0) {
            return new ArrayList<>();
        }

        // 基于用户的协同过滤
        Map<Long, Double> similarUsers = findSimilarUsersWithSimilarity(userId, 15, config);
        if (similarUsers.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取相似用户喜欢的景点
        List<Destination> destinations = destinationMapper.selectBySimilarUsers(new ArrayList<>(similarUsers.keySet()), userId, limit);
        return destinations.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("name", d.getName());
            map.put("locationId", d.getLocationId());
            
            // 获取地点名称
            if (d.getLocationId() != null) {
                Location location = locationMapper.findById(d.getLocationId());
                if (location != null) {
                    map.put("locationName", location.getName());
                    map.put("city", location.getName());
                    // 获取省份信息
                    if (location.getParentId() != null) {
                        Location province = locationMapper.findById(location.getParentId());
                        if (province != null) {
                            map.put("province", province.getName());
                        }
                    }
                }
            } else {
                map.put("province", "");
                map.put("city", "");
            }
            
            map.put("imageUrl", d.getImageUrl());
            map.put("ticketPrice", d.getTicketPrice());
            map.put("averageRating", d.getAverageRating());
            map.put("reason", "基于相似用户推荐");
            
            // 获取相似用户的最大相似度
            double maxSimilarity = similarUsers.values().stream().max(Double::compare).orElse(0.0);
            map.put("matchRate", calculateMatchRate(d, userPreferences, maxSimilarity));
            
            // 添加标签信息
            List<Tag> tags = tagMapper.selectByDestinationId(d.getId());
            map.put("tags", tags.stream().map(this::convertToTagVO).collect(Collectors.toList()));
            
            // 获取分类信息
            List<Long> categoryIds = destinationMapper.selectCategoryIdsByDestinationId(d.getId());
            List<CategoryVO> categories = new ArrayList<>();
            for (Long categoryId : categoryIds) {
                Category category = destinationMapper.selectCategoryById(categoryId);
                if (category != null) {
                    categories.add(convertToCategoryVO(category));
                }
            }
            map.put("categories", categories);
            
            return map;
        }).collect(Collectors.toList());
    }

    private Map<Long, Double> findSimilarUsersWithSimilarity(Long userId, int limit, RecommendationConfig config) {
        // 获取用户评分
        List<Rating> userRatings = ratingMapper.selectByUserId(userId);
        
        // 构建用户评分映射
        Map<Long, Integer> userRatingMap = new HashMap<>();
        for (Rating rating : userRatings) {
            userRatingMap.put(rating.getDestinationId(), rating.getScore());
        }
        
        // 获取用户行为数据
        List<UserBehavior> userBehaviors = userBehaviorMapper.selectByUserId(userId);
        Map<Long, Integer> userBehaviorMap = new HashMap<>();
        for (UserBehavior behavior : userBehaviors) {
            // 行为权重：收藏=3，分享=2，浏览=1
            int weight = 1;
            if ("collect".equals(behavior.getBehaviorType())) {
                weight = 3;
            } else if ("share".equals(behavior.getBehaviorType())) {
                weight = 2;
            }
            userBehaviorMap.merge(behavior.getDestinationId(), weight, Integer::sum);
        }
        
        if (userRatingMap.isEmpty() && userBehaviorMap.isEmpty()) {
            return new HashMap<>();
        }
        
        // 获取用户偏好标签
        List<Long> userPreferences = tagMapper.selectPreferenceTagIdsByUserId(userId);
        
        // 获取用户信息
        User currentUser = userMapper.findById(userId);
        if (currentUser == null) {
            return new HashMap<>();
        }
        
        // 联合候选：共同评分 + 共同行为，避免数据稀疏时无候选
        Set<Long> candidateUserIds = new HashSet<>(ratingMapper.findUsersWithCommonRatings(userId));
        candidateUserIds.addAll(userBehaviorMapper.findUsersWithCommonBehaviors(userId));
        if (candidateUserIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 计算相似度并排序
        List<Map.Entry<Long, Double>> similarityList = new ArrayList<>();
        for (Long candidateId : candidateUserIds) {
            // 获取候选用户评分
            List<Rating> candidateRatings = ratingMapper.selectByUserId(candidateId);
            Map<Long, Integer> candidateRatingMap = new HashMap<>();
            for (Rating rating : candidateRatings) {
                candidateRatingMap.put(rating.getDestinationId(), rating.getScore());
            }
            
            // 获取候选用户行为
            List<UserBehavior> candidateBehaviors = userBehaviorMapper.selectByUserId(candidateId);
            Map<Long, Integer> candidateBehaviorMap = new HashMap<>();
            for (UserBehavior behavior : candidateBehaviors) {
                // 行为权重：收藏=3，分享=2，浏览=1
                int weight = 1;
                if ("collect".equals(behavior.getBehaviorType())) {
                    weight = 3;
                } else if ("share".equals(behavior.getBehaviorType())) {
                    weight = 2;
                }
                candidateBehaviorMap.merge(behavior.getDestinationId(), weight, Integer::sum);
            }
            
            // 获取候选用户偏好标签
            List<Long> candidatePreferences = tagMapper.selectPreferenceTagIdsByUserId(candidateId);
            
            // 获取候选用户信息
            User candidateUser = userMapper.findById(candidateId);
            if (candidateUser == null) {
                continue;
            }
            
            // 计算综合相似度
            double similarity = SimilarityCalculator.calculateComprehensiveUserSimilarity(
                userRatingMap, candidateRatingMap,
                userBehaviorMap, candidateBehaviorMap,
                userPreferences, candidatePreferences,
                currentUser, candidateUser,
                config.getUserSimilarityRatingWeight(),
                config.getUserSimilarityBehaviorWeight(),
                config.getUserSimilarityPreferenceWeight(),
                config.getUserSimilarityAttributeWeight()
            );
            
            if (similarity > 0) {
                similarityList.add(new AbstractMap.SimpleEntry<>(candidateId, similarity));
            }
        }
        
        // 按相似度排序并返回
        similarityList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<Long, Double> result = new HashMap<>();
        for (Map.Entry<Long, Double> entry : similarityList.stream().limit(limit).collect(Collectors.toList())) {
            result.put(entry.getKey(), entry.getValue());
        }
        
        return result;
    }

    private List<Map<String, Object>> getContentBasedRecommendations(Long userId, int limit, RecommendationConfig config) {
        if (limit <= 0) {
            return new ArrayList<>();
        }
        // 获取用户收藏和浏览的景点
        List<Long> userDestinationIds = userBehaviorMapper.selectCollectedAndViewedDestinations(userId);
        if (userDestinationIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取用户偏好标签
        List<Long> userPreferences = tagMapper.selectPreferenceTagIdsByUserId(userId);
        
        // 请求级缓存，避免推荐过程中的重复查库
        Map<Long, Destination> destinationCache = new HashMap<>();
        Map<Long, Location> locationCache = new HashMap<>();
        Map<Long, List<Long>> tagIdsCache = new HashMap<>();
        Map<Long, List<Long>> categoryIdsCache = new HashMap<>();
        Map<Long, Map<Long, Integer>> ratingMapCache = new HashMap<>();
        
        // 计算景点相似度
        Map<Long, Double> similarityMap = new HashMap<>();
        for (Long destId : userDestinationIds) {
            List<Map<Long, Double>> similarDestinations = findSimilarDestinations(
                    destId, 10, config, destinationCache, locationCache, tagIdsCache, categoryIdsCache, ratingMapCache
            );
            for (Map<Long, Double> similarDest : similarDestinations) {
                for (Map.Entry<Long, Double> entry : similarDest.entrySet()) {
                    similarityMap.merge(entry.getKey(), entry.getValue(), Double::sum);
                }
            }
        }
        
        // 过滤已访问的景点并排序
        similarityMap.entrySet().removeIf(entry -> userDestinationIds.contains(entry.getKey()));
        List<Map.Entry<Long, Double>> sortedDestinations = new ArrayList<>(similarityMap.entrySet());
        sortedDestinations.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // 获取推荐结果
        List<Long> recommendedDestIds = sortedDestinations.stream()
                .limit(limit)   
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());  
        if (recommendedDestIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return destinationMapper.selectByIds(recommendedDestIds).stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("name", d.getName());
            map.put("locationId", d.getLocationId());
            
            // 使用缓存获取地点名称
            map.put("locationName", getLocationNameCached(d.getLocationId(), locationCache));
            
            map.put("imageUrl", d.getImageUrl());
            map.put("ticketPrice", d.getTicketPrice());
            map.put("averageRating", d.getAverageRating());
            map.put("reason", "基于相似景点推荐");
            map.put("matchRate", calculateMatchRate(d, userPreferences, 0));
            
            // 使用缓存获取标签信息
            List<Tag> tags = getTagsByDestinationIdCached(d.getId(), tagIdsCache);
            map.put("tags", tags.stream().map(this::convertToTagVO).collect(Collectors.toList()));
            
            // 获取分类信息
            List<Long> categoryIds = getCategoryIdsCached(d.getId(), categoryIdsCache);
            List<CategoryVO> categories = new ArrayList<>();
            for (Long categoryId : categoryIds) {
                Category category = destinationMapper.selectCategoryById(categoryId);
                if (category != null) {
                    categories.add(convertToCategoryVO(category));
                }
            }
            map.put("categories", categories);
            
            return map;
        }).collect(Collectors.toList());
    }

    private List<Map<Long, Double>> findSimilarDestinations(Long destinationId,
                                                            int limit,
                                                            RecommendationConfig config,
                                                            Map<Long, Destination> destinationCache,
                                                            Map<Long, Location> locationCache,
                                                            Map<Long, List<Long>> tagIdsCache,
                                                            Map<Long, List<Long>> categoryIdsCache,
                                                            Map<Long, Map<Long, Integer>> ratingMapCache) {
        Destination sourceDestination = getDestinationCached(destinationId, destinationCache);
        if (sourceDestination == null) {
            return new ArrayList<>();
        }
        
        Map<Long, Integer> destRatingMap = getDestinationRatingMapCached(destinationId, ratingMapCache);
        List<Long> destTags = getTagIdsCached(destinationId, tagIdsCache);
        List<Long> destCategories = getCategoryIdsCached(destinationId, categoryIdsCache);
        List<Long> locationRecallIds = buildCityProvinceRecallLocationIds(sourceDestination.getLocationId());
        int recallLimit = Math.max(limit * 10, 80);
        
        // 扩召回：共同评分 + 同标签 + 同分类 + 同市/同省
        Set<Long> candidateDestIds = new HashSet<>(ratingMapper.findDestinationsWithCommonRatings(destinationId));
        candidateDestIds.addAll(destinationMapper.selectDestinationIdsByTagIds(destTags, destinationId, recallLimit));
        candidateDestIds.addAll(destinationMapper.selectDestinationIdsByCategoryIds(destCategories, destinationId, recallLimit));
        candidateDestIds.addAll(destinationMapper.selectDestinationIdsByLocationIds(locationRecallIds, destinationId, recallLimit));
        if (candidateDestIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 计算相似度并排序
        List<Map.Entry<Long, Double>> similarityList = new ArrayList<>();
        for (Long candidateId : candidateDestIds) {
            Destination candidate = getDestinationCached(candidateId, destinationCache);
            if (candidate == null) {
                continue;
            }
            
            Map<Long, Integer> candidateRatingMap = getDestinationRatingMapCached(candidateId, ratingMapCache);
            
            double ratingSimilarity = 0.0;
            if (!destRatingMap.isEmpty() && !candidateRatingMap.isEmpty()) {
                ratingSimilarity = SimilarityCalculator.calculateDestinationSimilarity(destRatingMap, candidateRatingMap);
            }
            
            double tagSimilarity = 0.0;
            List<Long> candidateTags = getTagIdsCached(candidateId, tagIdsCache);
            tagSimilarity = SimilarityCalculator.calculateTagSimilarity(destTags, candidateTags);
            
            double categorySimilarity = 0.0;
            List<Long> candidateCategories = getCategoryIdsCached(candidateId, categoryIdsCache);
            categorySimilarity = SimilarityCalculator.calculateCategorySimilarity(destCategories, candidateCategories);
            
            double locationSimilarity = calculateLocationSimilarity(sourceDestination.getLocationId(), candidate.getLocationId(), locationCache);
            
            double compositeSimilarity = SimilarityCalculator.calculateCompositeSimilarity(
                    ratingSimilarity,
                    tagSimilarity,
                    categorySimilarity,
                    config.getDestinationSimilarityRatingWeight(),
                    config.getDestinationSimilarityTagWeight(),
                    config.getDestinationSimilarityCategoryWeight()
            );
            // 将地点相似度并入综合相似度，按配置权重融合
            double locationWeight = config.getDestinationSimilarityLocationWeight() == null ? 0.2 : config.getDestinationSimilarityLocationWeight();
            double baseWeight = 1.0 - locationWeight;
            if (baseWeight < 0) {
                baseWeight = 0;
            }
            compositeSimilarity = compositeSimilarity * baseWeight + locationSimilarity * locationWeight;
            
            if (compositeSimilarity > 0) {
                similarityList.add(new AbstractMap.SimpleEntry<>(candidateId, compositeSimilarity));
            }
        }
        
        // 按相似度排序并返回
        similarityList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        List<Map<Long, Double>> result = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : similarityList.stream().limit(limit).collect(Collectors.toList())) {
            Map<Long, Double> map = new HashMap<>();
            map.put(entry.getKey(), entry.getValue());
            result.add(map);
        }
        
        return result;
    }
    
    private List<Long> buildCityProvinceRecallLocationIds(Long locationId) {
        List<Long> locationIds = new ArrayList<>();
        if (locationId == null) {
            return locationIds;
        }
        Location current = locationMapper.findById(locationId);
        if (current == null) {
            return locationIds;
        }
        // 只有省/市层级：召回同市 + 同省下城市
        if (current.getLevel() != null && current.getLevel() == 2) {
            locationIds.add(current.getId());
            if (current.getParentId() != null) {
                List<Location> sameProvinceCities = locationMapper.selectByParentId(current.getParentId());
                for (Location city : sameProvinceCities) {
                    locationIds.add(city.getId());
                }
            }
        } else {
            locationIds.add(current.getId());
            List<Location> provinceCities = locationMapper.selectByParentId(current.getId());
            for (Location city : provinceCities) {
                locationIds.add(city.getId());
            }
        }
        return locationIds;
    }
    
    private double calculateLocationSimilarity(Long sourceLocationId, Long candidateLocationId, Map<Long, Location> locationCache) {
        if (sourceLocationId == null) {
            return 0.0;
        }
        if (candidateLocationId == null) {
            return 0.0;
        }
        if (Objects.equals(sourceLocationId, candidateLocationId)) {
            return 1.0;
        }
        Location l1 = getLocationCached(sourceLocationId, locationCache);
        Location l2 = getLocationCached(candidateLocationId, locationCache);
        if (l1 == null || l2 == null) {
            return 0.0;
        }

        Long province1 = (l1.getLevel() != null && l1.getLevel() == 1) ? l1.getId() : l1.getParentId();
        Long province2 = (l2.getLevel() != null && l2.getLevel() == 1) ? l2.getId() : l2.getParentId();
        // 仅保留省/市层级语义：同市1.0，同省0.6，其它0
        if (province1 != null && Objects.equals(province1, province2)) {
            return 0.6;
        }
        return 0.0;
    }
    
    private Destination getDestinationCached(Long destinationId, Map<Long, Destination> destinationCache) {
        return destinationCache.computeIfAbsent(destinationId, destinationMapper::findById);
    }
    
    private Location getLocationCached(Long locationId, Map<Long, Location> locationCache) {
        if (locationId == null) {
            return null;
        }
        return locationCache.computeIfAbsent(locationId, locationMapper::findById);
    }
    
    private String getLocationNameCached(Long locationId, Map<Long, Location> locationCache) {
        Location location = getLocationCached(locationId, locationCache);
        return location == null ? null : location.getName();
    }
    
    private Map<Long, Integer> getDestinationRatingMapCached(Long destinationId, Map<Long, Map<Long, Integer>> ratingMapCache) {
        return ratingMapCache.computeIfAbsent(destinationId, id -> {
            List<Rating> ratings = ratingMapper.selectByDestinationId(id);
            Map<Long, Integer> ratingMap = new HashMap<>();
            for (Rating rating : ratings) {
                ratingMap.put(rating.getUserId(), rating.getScore());
            }
            return ratingMap;
        });
    }
    
    private List<Long> getTagIdsCached(Long destinationId, Map<Long, List<Long>> tagIdsCache) {
        return tagIdsCache.computeIfAbsent(destinationId, tagMapper::selectTagIdsByDestinationId);
    }
    
    private List<Long> getCategoryIdsCached(Long destinationId, Map<Long, List<Long>> categoryIdsCache) {
        return categoryIdsCache.computeIfAbsent(destinationId, tagMapper::selectCategoryIdsByDestinationId);
    }
    
    private List<Tag> getTagsByDestinationIdCached(Long destinationId, Map<Long, List<Long>> tagIdsCache) {
        List<Long> tagIds = getTagIdsCached(destinationId, tagIdsCache);
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Tag> tags = new ArrayList<>();
        for (Long tagId : tagIds) {
            Tag tag = tagMapper.findById(tagId);
            if (tag != null) {
                tags.add(tag);
            }
        }
        return tags;
    }
    // 计算景点匹配度
    // 1. 标签匹配度（权重35%）
    // 2. 用户相似度（权重35%）
    // 3. 景点评分（权重20%）
    // 4. 景点热度（权重10%）
    private int calculateMatchRate(Destination destination, List<Long> userPreferences, double userSimilarity) {
        int matchScore = 0;
        int totalWeight = 0;
        
        // 1. 标签匹配度（权重35%）
        if (userPreferences != null && !userPreferences.isEmpty()) {
            List<Long> destinationTagIds = tagMapper.selectTagIdsByDestinationId(destination.getId());
            long matchedTags = destinationTagIds.stream()
                    .filter(userPreferences::contains)
                    .count();
            double tagMatchRate = (matchedTags * 100.0) / userPreferences.size();
            matchScore += (int)(tagMatchRate * 0.35);
            totalWeight += 35;
        }
        
        // 2. 用户相似度（权重35%）  
        if (userSimilarity > 0) {
            int similarityRate = (int)(userSimilarity * 100);
            matchScore += (int)(similarityRate * 0.35);
            totalWeight += 35;
        }
        
        // 3. 景点评分（权重20%）
        if (destination.getAverageRating() != null && destination.getAverageRating().doubleValue() > 0) {
            int ratingRate = (int)((destination.getAverageRating().doubleValue() / 5.0) * 100);
            matchScore += (int)(ratingRate * 0.2);
            totalWeight += 20;
        }
        
        // 4. 景点热度（权重10%）
        int popularityScore = 0;
        if (destination.getViewCount() != null && destination.getViewCount() > 0) {
            popularityScore += Math.min(destination.getViewCount() / 10, 50);
        }
        if (destination.getCollectCount() != null && destination.getCollectCount() > 0) {
            popularityScore += Math.min(destination.getCollectCount() / 5, 50);
        }
        popularityScore = Math.min(popularityScore, 100);
        matchScore += (int)(popularityScore * 0.1);
        totalWeight += 10;
        
        // 计算最终匹配度
        int finalMatchRate = totalWeight > 0 ? (matchScore * 100) / totalWeight : 50;
        
        return Math.min(finalMatchRate, 100);
    }

    public List<Map<String, Object>> getRecommendedForYou(Long userId, int limit) {
        return getRecommendedDestinations(userId, limit);
    }
}
