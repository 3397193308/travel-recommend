package com.example.demo.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserCollectionMapper {

    @Insert("INSERT INTO user_collections (user_id, destination_id) VALUES (#{userId}, #{destinationId})")
    int addCollection(@Param("userId") Long userId, @Param("destinationId") Long destinationId);

    @Delete("DELETE FROM user_collections WHERE user_id = #{userId} AND destination_id = #{destinationId}")
    int removeCollection(@Param("userId") Long userId, @Param("destinationId") Long destinationId);

    @Select("SELECT destination_id FROM user_collections WHERE user_id = #{userId}")
    List<Long> selectCollectionDestinationIds(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_collections WHERE user_id = #{userId} AND destination_id = #{destinationId}")
    int checkCollection(@Param("userId") Long userId, @Param("destinationId") Long destinationId);

    @Select("SELECT d.* FROM destinations d INNER JOIN user_collections uc ON d.id = uc.destination_id WHERE uc.user_id = #{userId} AND d.status = 1 ORDER BY uc.created_at DESC")
    List<java.util.Map<String, Object>> selectUserCollections(@Param("userId") Long userId);
}
