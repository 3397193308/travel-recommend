package com.example.demo.mapper;

import com.example.demo.entity.UserBehavior;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserBehaviorMapper {

    @Select("SELECT * FROM user_behaviors WHERE user_id = #{userId} AND destination_id = #{destinationId} AND behavior_type = #{behaviorType}")
    UserBehavior findByUserAndDestination(@Param("userId") Long userId, @Param("destinationId") Long destinationId, @Param("behaviorType") String behaviorType);

    @Insert("INSERT INTO user_behaviors (user_id, destination_id, behavior_type, create_time) VALUES (#{userId}, #{destinationId}, #{behaviorType}, NOW())")
    int insert(UserBehavior behavior);

    @Delete("DELETE FROM user_behaviors WHERE user_id = #{userId} AND destination_id = #{destinationId} AND behavior_type = #{behaviorType}")
    int delete(@Param("userId") Long userId, @Param("destinationId") Long destinationId, @Param("behaviorType") String behaviorType);

    @Select("SELECT destination_id FROM user_behaviors WHERE user_id = #{userId} AND behavior_type = 'collect' ORDER BY create_time DESC")
    List<Long> selectCollectedDestinationIds(@Param("userId") Long userId);

    @Select("SELECT * FROM user_behaviors WHERE user_id = #{userId} AND behavior_type = 'view' ORDER BY create_time DESC LIMIT #{limit}")
    List<UserBehavior> selectRecentViews(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Select("SELECT DISTINCT destination_id FROM user_behaviors WHERE user_id = #{userId} AND behavior_type IN ('collect', 'view')")
    List<Long> selectCollectedAndViewedDestinations(@Param("userId") Long userId);
    
    @Select("SELECT * FROM user_behaviors WHERE user_id = #{userId}")
    List<UserBehavior> selectByUserId(@Param("userId") Long userId);
    
    @Select("<script>" +
            "SELECT DISTINCT ub.user_id " +
            "FROM user_behaviors ub " +
            "WHERE ub.destination_id IN (" +
            "SELECT destination_id FROM user_behaviors WHERE user_id = #{userId}" +
            ") AND ub.user_id != #{userId} " +
            "AND ub.behavior_type IN ('collect', 'view', 'share')" +
            "</script>")
    List<Long> findUsersWithCommonBehaviors(@Param("userId") Long userId);
}
