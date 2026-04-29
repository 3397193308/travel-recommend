package com.example.demo.mapper;

import com.example.demo.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("SELECT c.*, u.username, u.avatar as userAvatar FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.destination_id = #{destinationId} AND c.status = 1 " +
            "ORDER BY c.create_time DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<Comment> selectByDestinationId(@Param("destinationId") Long destinationId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM comments WHERE destination_id = #{destinationId} AND status = 1")
    int countByDestinationId(@Param("destinationId") Long destinationId);

    @Insert("INSERT INTO comments (user_id, destination_id, content, parent_id, status, create_time, update_time) " +
            "VALUES (#{userId}, #{destinationId}, #{content}, #{parentId}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);

    @Select("SELECT c.*, u.username, u.avatar as userAvatar FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.id = #{id}")
    Comment findById(@Param("id") Long id);
}
