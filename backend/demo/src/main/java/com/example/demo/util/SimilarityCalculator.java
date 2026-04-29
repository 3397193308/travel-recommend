package com.example.demo.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.example.demo.entity.User;

public class SimilarityCalculator {

    /**
     * 计算用户余弦相似度
     * @param user1Ratings 用户1的评分映射
     * @param user2Ratings 用户2的评分映射
     * @return 余弦相似度值
     */
    public static double calculateUserSimilarity(Map<Long, Integer> user1Ratings, Map<Long, Integer> user2Ratings) {
        // 找出共同评分的景点
        Set<Long> commonDestinations = new HashSet<>(user1Ratings.keySet());
        commonDestinations.retainAll(user2Ratings.keySet());
        
        if (commonDestinations.isEmpty()) {
            return 0.0;
        }
        
        // 计算点积
        double dotProduct = 0.0;
        for (Long destId : commonDestinations) {
            dotProduct += user1Ratings.get(destId) * user2Ratings.get(destId);
        }
        
        // 计算用户1的向量模长
        double user1Norm = 0.0;
        for (Integer score : user1Ratings.values()) {
            user1Norm += score * score;
        }
        user1Norm = Math.sqrt(user1Norm);
        
        // 计算用户2的向量模长
        double user2Norm = 0.0;
        for (Integer score : user2Ratings.values()) {
            user2Norm += score * score;
        }
        user2Norm = Math.sqrt(user2Norm);
        
        // 计算余弦相似度
        if (user1Norm == 0 || user2Norm == 0) {
            return 0.0;
        }
        
        return dotProduct / (user1Norm * user2Norm);
    }
    
    /**
     * 计算景点余弦相似度（基于评分）
     * @param dest1Ratings 景点1的评分映射
     * @param dest2Ratings 景点2的评分映射
     * @return 余弦相似度值
     */
    public static double calculateDestinationSimilarity(Map<Long, Integer> dest1Ratings, Map<Long, Integer> dest2Ratings) {
        // 找出共同评分的用户
        Set<Long> commonUsers = new HashSet<>(dest1Ratings.keySet());
        commonUsers.retainAll(dest2Ratings.keySet());
        
        if (commonUsers.isEmpty()) {
            return 0.0;
        }
        
        // 计算点积
        double dotProduct = 0.0;
        for (Long userId : commonUsers) {
            dotProduct += dest1Ratings.get(userId) * dest2Ratings.get(userId);
        }
        
        // 计算景点1的向量模长
        double dest1Norm = 0.0;
        for (Integer score : dest1Ratings.values()) {
            dest1Norm += score * score;
        }
        dest1Norm = Math.sqrt(dest1Norm);
        
        // 计算景点2的向量模长
        double dest2Norm = 0.0;
        for (Integer score : dest2Ratings.values()) {
            dest2Norm += score * score;
        }
        dest2Norm = Math.sqrt(dest2Norm);
        
        // 计算余弦相似度
        if (dest1Norm == 0 || dest2Norm == 0) {
            return 0.0;
        }
        
        return dotProduct / (dest1Norm * dest2Norm);
    }
    
    /**
     * 计算标签相似度
     * @param tags1 景点1的标签ID列表
     * @param tags2 景点2的标签ID列表
     * @return 余弦相似度值
     */
    public static double calculateTagSimilarity(java.util.List<Long> tags1, java.util.List<Long> tags2) {
        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.0;
        }
        
        // 构建标签向量
        Map<Long, Integer> tagVector1 = new HashMap<>();
        Map<Long, Integer> tagVector2 = new HashMap<>();
        
        // 标记景点1的标签
        for (Long tagId : tags1) {
            tagVector1.put(tagId, 1);
        }
        
        // 标记景点2的标签
        for (Long tagId : tags2) {
            tagVector2.put(tagId, 1);
        }
        
        // 找出共同标签
        Set<Long> commonTags = new HashSet<>(tagVector1.keySet());
        commonTags.retainAll(tagVector2.keySet());
        
        // 计算点积
        double dotProduct = commonTags.size();
        
        // 计算向量模长
        double norm1 = Math.sqrt(tagVector1.size());
        double norm2 = Math.sqrt(tagVector2.size());
        
        // 计算余弦相似度
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (norm1 * norm2);
    }
    
    /**
     * 计算类别相似度
     * @param categories1 景点1的类别ID列表
     * @param categories2 景点2的类别ID列表
     * @return 余弦相似度值
     */
    public static double calculateCategorySimilarity(java.util.List<Long> categories1, java.util.List<Long> categories2) {
        return calculateTagSimilarity(categories1, categories2);
    }
    
    /**
     * 综合相似度计算
     * @param ratingSimilarity 评分相似度
     * @param tagSimilarity 标签相似度
     * @param categorySimilarity 类别相似度
     * @return 综合相似度
     */
    public static double calculateCompositeSimilarity(double ratingSimilarity, double tagSimilarity, double categorySimilarity) {
        return calculateCompositeSimilarity(ratingSimilarity, tagSimilarity, categorySimilarity, 0.5, 0.3, 0.2);
    }
    
    public static double calculateCompositeSimilarity(double ratingSimilarity, double tagSimilarity, double categorySimilarity,
                                                      double ratingWeight, double tagWeight, double categoryWeight) {
        double weightSum = ratingWeight + tagWeight + categoryWeight;
        if (weightSum <= 0) {
            return 0.0;
        }
        return (ratingWeight * ratingSimilarity + tagWeight * tagSimilarity + categoryWeight * categorySimilarity) / weightSum;
    }
    
    /**
     * 计算用户行为相似度
     * @param user1Behaviors 用户1的行为映射（景点ID -> 行为权重）
     * @param user2Behaviors 用户2的行为映射（景点ID -> 行为权重）
     * @return 行为相似度值
     */
    public static double calculateBehaviorSimilarity(Map<Long, Integer> user1Behaviors, Map<Long, Integer> user2Behaviors) {
        // 找出共同行为的景点
        Set<Long> commonDestinations = new HashSet<>(user1Behaviors.keySet());
        commonDestinations.retainAll(user2Behaviors.keySet());
        
        if (commonDestinations.isEmpty()) {
            return 0.0;
        }
        
        // 计算点积
        double dotProduct = 0.0;
        for (Long destId : commonDestinations) {
            dotProduct += user1Behaviors.get(destId) * user2Behaviors.get(destId);
        }
        
        // 计算用户1的行为向量模长
        double user1Norm = 0.0;
        for (Integer weight : user1Behaviors.values()) {
            user1Norm += weight * weight;
        }
        user1Norm = Math.sqrt(user1Norm);
        
        // 计算用户2的行为向量模长
        double user2Norm = 0.0;
        for (Integer weight : user2Behaviors.values()) {
            user2Norm += weight * weight;
        }
        user2Norm = Math.sqrt(user2Norm);
        
        // 计算余弦相似度
        if (user1Norm == 0 || user2Norm == 0) {
            return 0.0;
        }
        
        return dotProduct / (user1Norm * user2Norm);
    }
    
    /**
     * 计算用户属性相似度
     * @param user1 用户1
     * @param user2 用户2
     * @return 属性相似度值
     */
    public static double calculateUserAttributeSimilarity(User user1, User user2) {
        double similarity = 0.0;
        int attributeCount = 0;
        
        // 计算年龄相似度（相近年龄相似度高）
        if (user1.getAge() != null && user2.getAge() != null) {
            int ageDiff = Math.abs(user1.getAge() - user2.getAge());
            double ageSimilarity = Math.max(0, 1 - ageDiff / 50.0);
            similarity += ageSimilarity;
            attributeCount++;
        }
        
        // 计算性别相似度（相同性别相似度高）
        if (user1.getGender() != null && user2.getGender() != null) {
            double genderSimilarity = user1.getGender().equals(user2.getGender()) ? 1.0 : 0.5;
            similarity += genderSimilarity;
            attributeCount++;
        }
        
        // 计算地理位置相似度（相同地点相似度高）
        if (user1.getLocationId() != null && user2.getLocationId() != null) {
            double locationSimilarity = user1.getLocationId().equals(user2.getLocationId()) ? 1.0 : 0.3;
            similarity += locationSimilarity;
            attributeCount++;
        }
        
        return attributeCount > 0 ? similarity / attributeCount : 0.0;
    }
    
    /**
     * 计算综合用户相似度
     * @param user1Ratings 用户1的评分映射
     * @param user2Ratings 用户2的评分映射
     * @param user1Behaviors 用户1的行为映射
     * @param user2Behaviors 用户2的行为映射
     * @param user1Preferences 用户1的偏好标签列表
     * @param user2Preferences 用户2的偏好标签列表
     * @param user1 用户1
     * @param user2 用户2
     * @return 综合用户相似度
     */
    public static double calculateComprehensiveUserSimilarity(
        Map<Long, Integer> user1Ratings,
        Map<Long, Integer> user2Ratings,
        Map<Long, Integer> user1Behaviors,
        Map<Long, Integer> user2Behaviors,
        java.util.List<Long> user1Preferences,
        java.util.List<Long> user2Preferences,
        User user1,
        User user2
    ) {
        return calculateComprehensiveUserSimilarity(
                user1Ratings, user2Ratings, user1Behaviors, user2Behaviors, user1Preferences, user2Preferences, user1, user2,
                0.35, 0.3, 0.25, 0.1
        );
    }
    
    public static double calculateComprehensiveUserSimilarity(
        Map<Long, Integer> user1Ratings,
        Map<Long, Integer> user2Ratings,
        Map<Long, Integer> user1Behaviors,
        Map<Long, Integer> user2Behaviors,
        java.util.List<Long> user1Preferences,
        java.util.List<Long> user2Preferences,
        User user1,
        User user2,
        double ratingWeight,
        double behaviorWeight,
        double preferenceWeight,
        double attributeWeight
    ) {
        // 计算评分相似度
        double ratingSimilarity = calculateUserSimilarity(user1Ratings, user2Ratings);
        
        // 计算行为相似度
        double behaviorSimilarity = calculateBehaviorSimilarity(user1Behaviors, user2Behaviors);
        
        // 计算偏好相似度
        double preferenceSimilarity = calculateTagSimilarity(user1Preferences, user2Preferences);
        
        // 计算属性相似度
        double attributeSimilarity = calculateUserAttributeSimilarity(user1, user2);
        
        // 加权融合
        double weightSum = ratingWeight + behaviorWeight + preferenceWeight + attributeWeight;
        if (weightSum <= 0) {
            return 0.0;
        }
        return (ratingWeight * ratingSimilarity + behaviorWeight * behaviorSimilarity +
               preferenceWeight * preferenceSimilarity + attributeWeight * attributeSimilarity) / weightSum;
    }
}
