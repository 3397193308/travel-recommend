package com.example.demo.service;

import com.example.demo.entity.RecommendationConfig;
import com.example.demo.entity.Result;
import com.example.demo.mapper.RecommendationConfigMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecommendationConfigService {
    @Autowired
    private RecommendationConfigMapper recommendationConfigMapper;

    @PostConstruct
    public void init() {
        recommendationConfigMapper.createTableIfNotExists();
        try {
            recommendationConfigMapper.ensureLocationWeightColumn();
        } catch (Exception ignored) {
            // 兼容旧版本MySQL
        }
        recommendationConfigMapper.initDefaultIfEmpty();
    }

    public RecommendationConfig getCurrentConfig() {
        RecommendationConfig config = recommendationConfigMapper.getCurrentConfig();
        if (config == null) {
            recommendationConfigMapper.initDefaultIfEmpty();
            config = recommendationConfigMapper.getCurrentConfig();
        }
        return config;
    }

    public Result<RecommendationConfig> getConfigResult() {
        return Result.success(getCurrentConfig());
    }

    public Result<String> updateConfig(RecommendationConfig config) {
        RecommendationConfig current = getCurrentConfig();
        if (current == null) {
            return Result.error("算法配置不存在");
        }
        normalizeConfig(config);
        config.setId(current.getId());
        int affected = recommendationConfigMapper.updateConfig(config);
        if (affected <= 0) {
            return Result.error("更新算法配置失败");
        }
        return Result.success("更新成功");
    }
    
    private double sanitize(Double value, double fallback) {
        if (value == null || value < 0) {
            return fallback;
        }
        return value;
    }
    
    private void normalizeConfig(RecommendationConfig config) {
        config.setUserSimilarityRatingWeight(sanitize(config.getUserSimilarityRatingWeight(), 0.35));
        config.setUserSimilarityBehaviorWeight(sanitize(config.getUserSimilarityBehaviorWeight(), 0.30));
        config.setUserSimilarityPreferenceWeight(sanitize(config.getUserSimilarityPreferenceWeight(), 0.25));
        config.setUserSimilarityAttributeWeight(sanitize(config.getUserSimilarityAttributeWeight(), 0.10));
        
        config.setDestinationSimilarityRatingWeight(sanitize(config.getDestinationSimilarityRatingWeight(), 0.30));
        config.setDestinationSimilarityTagWeight(sanitize(config.getDestinationSimilarityTagWeight(), 0.30));
        config.setDestinationSimilarityCategoryWeight(sanitize(config.getDestinationSimilarityCategoryWeight(), 0.20));
        config.setDestinationSimilarityLocationWeight(sanitize(config.getDestinationSimilarityLocationWeight(), 0.30));
        
        config.setRecommendationCollaborativeWeight(sanitize(config.getRecommendationCollaborativeWeight(), 0.50));
        config.setRecommendationPreferenceWeight(sanitize(config.getRecommendationPreferenceWeight(), 0.25));
        config.setRecommendationContentWeight(sanitize(config.getRecommendationContentWeight(), 0.20));
        config.setRecommendationHotWeight(sanitize(config.getRecommendationHotWeight(), 0.05));
    }
}
