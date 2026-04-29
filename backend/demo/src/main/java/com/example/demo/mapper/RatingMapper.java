package com.example.demo.mapper;

import com.example.demo.entity.Rating;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface RatingMapper {

    @Select("SELECT * FROM ratings WHERE user_id = #{userId} AND destination_id = #{destinationId}")
    Rating findByUserAndDestination(@Param("userId") Long userId, @Param("destinationId") Long destinationId);

    @Insert("INSERT INTO ratings (user_id, destination_id, score, create_time, update_time) VALUES (#{userId}, #{destinationId}, #{score}, NOW(), NOW())")
    int insert(Rating rating);

    @Update("UPDATE ratings SET score = #{score}, update_time = NOW() WHERE user_id = #{userId} AND destination_id = #{destinationId}")
    int updateScore(@Param("userId") Long userId, @Param("destinationId") Long destinationId, @Param("score") Integer score);

    @Select("SELECT IFNULL(AVG(score), 0) FROM ratings WHERE destination_id = #{destinationId}")
    Double getAverageScore(@Param("destinationId") Long destinationId);

    @Select("SELECT COUNT(*) FROM ratings WHERE destination_id = #{destinationId}")
    int countByDestination(@Param("destinationId") Long destinationId);

    @Update("UPDATE destinations SET average_rating = #{avgRating}, rating_count = #{count} WHERE id = #{destinationId}")
    int updateDestinationRating(@Param("destinationId") Long destinationId, @Param("avgRating") Double avgRating, @Param("count") Integer count);

    @Select("SELECT * FROM ratings WHERE user_id = #{userId}")
    List<Rating> selectByUserId(@Param("userId") Long userId);

    @Select("<script>" +
            "SELECT r.user_id, COUNT(*) as common_ratings " +
            "FROM ratings r " +
            "WHERE r.destination_id IN (" +
            "SELECT destination_id FROM ratings WHERE user_id = #{userId}" +
            ") AND r.user_id != #{userId} " +
            "GROUP BY r.user_id " +
            "ORDER BY common_ratings DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> findSimilarUsersWithCount(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT r.user_id " +
            "FROM ratings r " +
            "WHERE r.destination_id IN (" +
            "SELECT destination_id FROM ratings WHERE user_id = #{userId}" +
            ") AND r.user_id != #{userId} " +
            "GROUP BY r.user_id " +
            "ORDER BY COUNT(*) DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Long> findSimilarUsers(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Select("<script>" +
            "SELECT DISTINCT r.user_id " +
            "FROM ratings r " +
            "WHERE r.destination_id IN (" +
            "SELECT destination_id FROM ratings WHERE user_id = #{userId}" +
            ") AND r.user_id != #{userId}" +
            "</script>")
    List<Long> findUsersWithCommonRatings(@Param("userId") Long userId);
    
    @Select("SELECT * FROM ratings WHERE destination_id = #{destinationId}")
    List<Rating> selectByDestinationId(@Param("destinationId") Long destinationId);
    
    @Select("<script>" +
            "SELECT DISTINCT r.destination_id " +
            "FROM ratings r " +
            "WHERE r.user_id IN (" +
            "SELECT user_id FROM ratings WHERE destination_id = #{destinationId}" +
            ") AND r.destination_id != #{destinationId}" +
            "</script>")
    List<Long> findDestinationsWithCommonRatings(@Param("destinationId") Long destinationId);
}
