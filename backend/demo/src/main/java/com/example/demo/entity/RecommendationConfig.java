package com.example.demo.entity;

import lombok.Data;

@Data
public class RecommendationConfig {
    private Long id;
    private Double userSimilarityRatingWeight;
    private Double userSimilarityBehaviorWeight;
    private Double userSimilarityPreferenceWeight;
    private Double userSimilarityAttributeWeight;
    private Double destinationSimilarityRatingWeight;
    private Double destinationSimilarityTagWeight;
    private Double destinationSimilarityCategoryWeight;
    private Double destinationSimilarityLocationWeight;
    private Double recommendationCollaborativeWeight;
    private Double recommendationPreferenceWeight;
    private Double recommendationContentWeight;
    private Double recommendationHotWeight;
}
