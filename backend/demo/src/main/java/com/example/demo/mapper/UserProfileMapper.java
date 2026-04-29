package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserProfileMapper {
    @Select("SELECT COUNT(*) FROM user_collections WHERE user_id = #{userId}")
    int countCollections(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM ratings WHERE user_id = #{userId}")
    int countRatings(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM comments WHERE user_id = #{userId}")
    int countComments(@Param("userId") Long userId);

    @Select("SELECT ub.destination_id AS id, d.name, d.image_url AS imageUrl, l.name AS locationName, ub.create_time AS viewTime " +
            "FROM user_behaviors ub " +
            "INNER JOIN destinations d ON d.id = ub.destination_id " +
            "LEFT JOIN locations l ON l.id = d.location_id " +
            "WHERE ub.user_id = #{userId} AND ub.behavior_type = 'view' " +
            "ORDER BY ub.create_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectRecentViews(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT r.destination_id AS id, d.name, d.image_url AS imageUrl, r.score, r.create_time AS ratingTime, " +
            "(SELECT c.content FROM comments c WHERE c.user_id = r.user_id AND c.destination_id = r.destination_id ORDER BY c.create_time DESC LIMIT 1) AS comment " +
            "FROM ratings r " +
            "INNER JOIN destinations d ON d.id = r.destination_id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.update_time DESC LIMIT #{limit}")
    List<Map<String, Object>> selectMyRatings(@Param("userId") Long userId, @Param("limit") Integer limit);
}
