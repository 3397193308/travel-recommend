package com.example.demo.mapper;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Destination;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {

    @Select("SELECT id, username, password, real_name AS realName, role, status FROM admins WHERE username = #{username} LIMIT 1")
    Admin findAdminByUsername(@Param("username") String username);

    @Select("SELECT id, username, password, real_name AS realName, role, status FROM admins WHERE id = #{id} LIMIT 1")
    Admin findAdminById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM users")
    Long countUsers();

    @Select("SELECT COUNT(*) FROM destinations")
    Long countDestinations();

    @Select("SELECT COUNT(*) FROM comments")
    Long countComments();

    @Select("SELECT COUNT(*) FROM ratings")
    Long countRatings();

    @Select("SELECT id, username, status, create_time AS createTime FROM users ORDER BY id DESC LIMIT 8")
    List<Map<String, Object>> listRecentUsers();

    @Select("SELECT c.id, c.content, c.status, c.create_time AS createTime, u.username, d.name AS destinationName " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "LEFT JOIN destinations d ON c.destination_id = d.id " +
            "ORDER BY c.id DESC LIMIT 8")
    List<Map<String, Object>> listRecentComments();

    @Select("<script>" +
            "SELECT d.id, d.name, d.location_id AS locationId, l.name AS locationName, d.ticket_price AS ticketPrice, " +
            "d.average_rating AS averageRating, d.view_count AS viewCount, d.collect_count AS collectCount, d.status " +
            "FROM destinations d " +
            "LEFT JOIN locations l ON d.location_id = l.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND d.name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if> " +
            "<if test='status != null'> " +
            "AND d.status = #{status} " +
            "</if> " +
            "ORDER BY d.id DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Map<String, Object>> listDestinationsForAdmin(@Param("keyword") String keyword,
                                                       @Param("status") Integer status,
                                                       @Param("offset") Integer offset,
                                                       @Param("pageSize") Integer pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM destinations d " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND d.name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if> " +
            "<if test='status != null'> " +
            "AND d.status = #{status} " +
            "</if> " +
            "</script>")
    Long countDestinationsForAdmin(@Param("keyword") String keyword, @Param("status") Integer status);

    @Insert("INSERT INTO destinations (name, description, location_id, address, image_url, image_urls, ticket_price, average_rating, rating_count, view_count, collect_count, status) " +
            "VALUES (#{name}, #{description}, #{locationId}, #{address}, #{imageUrl}, #{imageUrls}, #{ticketPrice}, 0, 0, 0, 0, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDestination(Destination destination);

    @Update("UPDATE destinations SET name = #{name}, description = #{description}, location_id = #{locationId}, address = #{address}, " +
            "image_url = #{imageUrl}, image_urls = #{imageUrls}, ticket_price = #{ticketPrice}, status = #{status} WHERE id = #{id}")
    int updateDestination(Destination destination);

    @Update("UPDATE destinations SET status = #{status} WHERE id = #{id}")
    int updateDestinationStatus(@Param("id") Long id, @Param("status") Integer status);
    
    @Update("<script>" +
            "UPDATE destinations SET status = #{status} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int updateDestinationStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);

    @Delete("DELETE FROM destinations WHERE id = #{id}")
    int deleteDestination(@Param("id") Long id);

    @Delete("DELETE FROM destination_tags WHERE destination_id = #{destinationId}")
    int deleteDestinationTags(@Param("destinationId") Long destinationId);

    @Insert("INSERT INTO destination_tags (destination_id, tag_id) VALUES (#{destinationId}, #{tagId})")
    int insertDestinationTag(@Param("destinationId") Long destinationId, @Param("tagId") Long tagId);

    @Select("SELECT tag_id FROM destination_tags WHERE destination_id = #{destinationId}")
    List<Long> listDestinationTagIds(@Param("destinationId") Long destinationId);
    
    @Delete("DELETE FROM destination_category_ref WHERE destination_id = #{destinationId}")
    int deleteDestinationCategories(@Param("destinationId") Long destinationId);
    
    @Insert("INSERT INTO destination_category_ref (destination_id, category_id) VALUES (#{destinationId}, #{categoryId})")
    int insertDestinationCategory(@Param("destinationId") Long destinationId, @Param("categoryId") Long categoryId);
    
    @Select("SELECT category_id FROM destination_category_ref WHERE destination_id = #{destinationId}")
    List<Long> listDestinationCategoryIds(@Param("destinationId") Long destinationId);

    @Select("<script>" +
            "SELECT id, name, description, sort_order AS sortOrder, status, create_time AS createTime " +
            "FROM tags WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if> " +
            "ORDER BY sort_order ASC, id ASC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Tag> listTags(@Param("keyword") String keyword, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM tags WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if> " +
            "</script>")
    Long countTags(@Param("keyword") String keyword);

    @Insert("INSERT INTO tags (name, description, sort_order, status) VALUES (#{name}, #{description}, #{sortOrder}, #{status})")
    int insertTag(Tag tag);

    @Update("UPDATE tags SET name = #{name}, description = #{description}, sort_order = #{sortOrder}, status = #{status} WHERE id = #{id}")
    int updateTag(Tag tag);

    @Update("UPDATE tags SET status = #{status} WHERE id = #{id}")
    int updateTagStatus(@Param("id") Long id, @Param("status") Integer status);
    
    @Update("<script>" +
            "UPDATE tags SET status = #{status} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int updateTagStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT id, username, email, phone, status, create_time AS createTime " +
            "FROM users WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "<if test='status != null'> " +
            "AND status = #{status} " +
            "</if> " +
            "ORDER BY id DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<User> listUsers(@Param("keyword") String keyword,
                         @Param("status") Integer status,
                         @Param("offset") Integer offset,
                         @Param("pageSize") Integer pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM users WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "<if test='status != null'> " +
            "AND status = #{status} " +
            "</if> " +
            "</script>")
    Long countUsersForAdmin(@Param("keyword") String keyword, @Param("status") Integer status);

    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);
    
    @Update("<script>" +
            "UPDATE users SET status = #{status} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int updateUserStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT c.id, c.user_id AS userId, c.destination_id AS destinationId, c.content, c.status, c.create_time AS createTime, " +
            "u.username, d.name AS destinationName " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "LEFT JOIN destinations d ON c.destination_id = d.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (c.content LIKE CONCAT('%', #{keyword}, '%') OR u.username LIKE CONCAT('%', #{keyword}, '%') OR d.name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "<if test='status != null'> " +
            "AND c.status = #{status} " +
            "</if> " +
            "ORDER BY c.id DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Map<String, Object>> listComments(@Param("keyword") String keyword,
                                           @Param("status") Integer status,
                                           @Param("offset") Integer offset,
                                           @Param("pageSize") Integer pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM comments c LEFT JOIN users u ON c.user_id = u.id LEFT JOIN destinations d ON c.destination_id = d.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (c.content LIKE CONCAT('%', #{keyword}, '%') OR u.username LIKE CONCAT('%', #{keyword}, '%') OR d.name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "<if test='status != null'> " +
            "AND c.status = #{status} " +
            "</if> " +
            "</script>")
    Long countCommentsForAdmin(@Param("keyword") String keyword, @Param("status") Integer status);

    @Update("UPDATE comments SET status = #{status} WHERE id = #{id}")
    int updateCommentStatus(@Param("id") Long id, @Param("status") Integer status);
    
    @Update("<script>" +
            "UPDATE comments SET status = #{status} WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int updateCommentStatusBatch(@Param("ids") List<Long> ids, @Param("status") Integer status);
}
