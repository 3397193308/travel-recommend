package com.example.demo.mapper;

import com.example.demo.entity.RecommendationConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RecommendationConfigMapper {
    @Update("CREATE TABLE IF NOT EXISTS recommendation_config (" +
            "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
            "user_similarity_rating_weight DOUBLE NOT NULL DEFAULT 0.35," +
            "user_similarity_behavior_weight DOUBLE NOT NULL DEFAULT 0.30," +
            "user_similarity_preference_weight DOUBLE NOT NULL DEFAULT 0.25," +
            "user_similarity_attribute_weight DOUBLE NOT NULL DEFAULT 0.10," +
            "destination_similarity_rating_weight DOUBLE NOT NULL DEFAULT 0.30," +
            "destination_similarity_tag_weight DOUBLE NOT NULL DEFAULT 0.30," +
            "destination_similarity_category_weight DOUBLE NOT NULL DEFAULT 0.20," +
            "destination_similarity_location_weight DOUBLE NOT NULL DEFAULT 0.30," +
            "recommendation_collaborative_weight DOUBLE NOT NULL DEFAULT 0.50," +
            "recommendation_preference_weight DOUBLE NOT NULL DEFAULT 0.25," +
            "recommendation_content_weight DOUBLE NOT NULL DEFAULT 0.20," +
            "recommendation_hot_weight DOUBLE NOT NULL DEFAULT 0.05," +
            "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")")
    int createTableIfNotExists();
    
    @Update("ALTER TABLE recommendation_config " +
            "ADD COLUMN destination_similarity_location_weight DOUBLE NOT NULL DEFAULT 0.20")
    int ensureLocationWeightColumn();

    @Update("INSERT INTO recommendation_config (" +
            "user_similarity_rating_weight, user_similarity_behavior_weight, user_similarity_preference_weight, user_similarity_attribute_weight, " +
            "destination_similarity_rating_weight, destination_similarity_tag_weight, destination_similarity_category_weight, destination_similarity_location_weight, " +
            "recommendation_collaborative_weight, recommendation_preference_weight, recommendation_content_weight, recommendation_hot_weight" +
            ") " +
            "SELECT 0.35, 0.30, 0.25, 0.10, 0.30, 0.30, 0.20, 0.30, 0.50, 0.25, 0.20, 0.05 " +
            "FROM dual WHERE NOT EXISTS (SELECT 1 FROM recommendation_config)")
    int initDefaultIfEmpty();

    @Select("SELECT id, " +
            "user_similarity_rating_weight AS userSimilarityRatingWeight, " +
            "user_similarity_behavior_weight AS userSimilarityBehaviorWeight, " +
            "user_similarity_preference_weight AS userSimilarityPreferenceWeight, " +
            "user_similarity_attribute_weight AS userSimilarityAttributeWeight, " +
            "destination_similarity_rating_weight AS destinationSimilarityRatingWeight, " +
            "destination_similarity_tag_weight AS destinationSimilarityTagWeight, " +
            "destination_similarity_category_weight AS destinationSimilarityCategoryWeight, " +
            "destination_similarity_location_weight AS destinationSimilarityLocationWeight, " +
            "recommendation_collaborative_weight AS recommendationCollaborativeWeight, " +
            "recommendation_preference_weight AS recommendationPreferenceWeight, " +
            "recommendation_content_weight AS recommendationContentWeight, " +
            "recommendation_hot_weight AS recommendationHotWeight " +
            "FROM recommendation_config ORDER BY id DESC LIMIT 1")
    RecommendationConfig getCurrentConfig();

    @Update("UPDATE recommendation_config SET " +
            "user_similarity_rating_weight = #{userSimilarityRatingWeight}, " +
            "user_similarity_behavior_weight = #{userSimilarityBehaviorWeight}, " +
            "user_similarity_preference_weight = #{userSimilarityPreferenceWeight}, " +
            "user_similarity_attribute_weight = #{userSimilarityAttributeWeight}, " +
            "destination_similarity_rating_weight = #{destinationSimilarityRatingWeight}, " +
            "destination_similarity_tag_weight = #{destinationSimilarityTagWeight}, " +
            "destination_similarity_category_weight = #{destinationSimilarityCategoryWeight}, " +
            "destination_similarity_location_weight = #{destinationSimilarityLocationWeight}, " +
            "recommendation_collaborative_weight = #{recommendationCollaborativeWeight}, " +
            "recommendation_preference_weight = #{recommendationPreferenceWeight}, " +
            "recommendation_content_weight = #{recommendationContentWeight}, " +
            "recommendation_hot_weight = #{recommendationHotWeight} " +
            "WHERE id = #{id}")
    int updateConfig(RecommendationConfig config);
}
