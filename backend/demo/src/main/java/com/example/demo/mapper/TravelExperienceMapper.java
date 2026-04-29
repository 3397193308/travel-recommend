package com.example.demo.mapper;

import com.example.demo.dto.ExperienceQueryRequest;
import com.example.demo.entity.TravelExperience;
import com.example.demo.entity.TravelExperienceImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TravelExperienceMapper {

    @Select("SELECT COUNT(*) FROM destinations WHERE id = #{destinationId}")
    int countDestinationById(@Param("destinationId") Long destinationId);

    @Insert("INSERT INTO travel_experience (user_id, destination_id, title, content, star, status, create_time, update_time) " +
            "VALUES (#{userId}, #{destinationId}, #{title}, #{content}, #{star}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertExperience(TravelExperience experience);

    @Insert("INSERT INTO travel_experience_image (experience_id, image_url, sort, create_time) " +
            "VALUES (#{experienceId}, #{imageUrl}, #{sort}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertExperienceImage(TravelExperienceImage image);

    @Select("SELECT id, experience_id AS experienceId, image_url AS imageUrl, sort, create_time AS createTime " +
            "FROM travel_experience_image WHERE experience_id = #{experienceId} ORDER BY sort ASC, id ASC")
    List<TravelExperienceImage> selectImagesByExperienceId(@Param("experienceId") Long experienceId);

    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM travel_experience te " +
            "LEFT JOIN destinations d ON te.destination_id = d.id " +
            "LEFT JOIN locations city ON d.location_id = city.id " +
            "WHERE te.status = 1 " +
            "<if test='request.destinationId != null'> AND te.destination_id = #{request.destinationId} </if>" +
            "<if test='request.keyword != null and request.keyword != \"\"'> " +
            "AND (te.title LIKE CONCAT('%', #{request.keyword}, '%') OR te.content LIKE CONCAT('%', #{request.keyword}, '%')) " +
            "</if>" +
            "<if test='request.locationId != null'> " +
            "AND (city.id = #{request.locationId} OR city.parent_id = #{request.locationId}) " +
            "</if>" +
            "</script>")
    int countApprovedExperiences(@Param("request") ExperienceQueryRequest request);

    @Select("<script>" +
            "SELECT te.id, te.user_id AS userId, te.destination_id AS destinationId, te.title, te.content, te.star, te.status, " +
            "te.reject_reason AS rejectReason, te.create_time AS createTime, te.update_time AS updateTime, " +
            "u.username, d.name AS destinationName, city.name AS cityName, province.name AS provinceName " +
            "FROM travel_experience te " +
            "LEFT JOIN users u ON te.user_id = u.id " +
            "LEFT JOIN destinations d ON te.destination_id = d.id " +
            "LEFT JOIN locations city ON d.location_id = city.id " +
            "LEFT JOIN locations province ON (CASE WHEN city.level = 1 THEN city.id ELSE city.parent_id END) = province.id " +
            "WHERE te.status = 1 " +
            "<if test='request.destinationId != null'> AND te.destination_id = #{request.destinationId} </if>" +
            "<if test='request.keyword != null and request.keyword != \"\"'> " +
            "AND (te.title LIKE CONCAT('%', #{request.keyword}, '%') OR te.content LIKE CONCAT('%', #{request.keyword}, '%')) " +
            "</if>" +
            "<if test='request.locationId != null'> " +
            "AND (city.id = #{request.locationId} OR city.parent_id = #{request.locationId}) " +
            "</if>" +
            "ORDER BY te.id DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<TravelExperience> selectApprovedExperiences(@Param("request") ExperienceQueryRequest request,
                                                     @Param("offset") int offset,
                                                     @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM travel_experience te WHERE te.user_id = #{userId} " +
            "<if test='status != null'> AND te.status = #{status} </if>" +
            "</script>")
    int countMyExperiences(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT te.id, te.user_id AS userId, te.destination_id AS destinationId, te.title, te.content, te.star, te.status, " +
            "te.reject_reason AS rejectReason, te.create_time AS createTime, te.update_time AS updateTime, " +
            "u.username, d.name AS destinationName, city.name AS cityName, province.name AS provinceName " +
            "FROM travel_experience te " +
            "LEFT JOIN users u ON te.user_id = u.id " +
            "LEFT JOIN destinations d ON te.destination_id = d.id " +
            "LEFT JOIN locations city ON d.location_id = city.id " +
            "LEFT JOIN locations province ON (CASE WHEN city.level = 1 THEN city.id ELSE city.parent_id END) = province.id " +
            "WHERE te.user_id = #{userId} " +
            "<if test='status != null'> AND te.status = #{status} </if>" +
            "ORDER BY te.id DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<TravelExperience> selectMyExperiences(@Param("userId") Long userId,
                                               @Param("status") Integer status,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    @Select("SELECT te.id, te.user_id AS userId, te.destination_id AS destinationId, te.title, te.content, te.star, te.status, " +
            "te.reject_reason AS rejectReason, te.create_time AS createTime, te.update_time AS updateTime, " +
            "u.username, d.name AS destinationName, city.name AS cityName, province.name AS provinceName " +
            "FROM travel_experience te " +
            "LEFT JOIN users u ON te.user_id = u.id " +
            "LEFT JOIN destinations d ON te.destination_id = d.id " +
            "LEFT JOIN locations city ON d.location_id = city.id " +
            "LEFT JOIN locations province ON (CASE WHEN city.level = 1 THEN city.id ELSE city.parent_id END) = province.id " +
            "WHERE te.id = #{id} LIMIT 1")
    TravelExperience selectExperienceById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT COUNT(*) FROM travel_experience te " +
            "LEFT JOIN users u ON te.user_id = u.id " +
            "LEFT JOIN destinations d ON te.destination_id = d.id " +
            "WHERE 1=1 " +
            "<if test='request.status != null'> AND te.status = #{request.status} </if>" +
            "<if test='request.keyword != null and request.keyword != \"\"'> " +
            "AND (te.title LIKE CONCAT('%', #{request.keyword}, '%') OR te.content LIKE CONCAT('%', #{request.keyword}, '%') " +
            "OR u.username LIKE CONCAT('%', #{request.keyword}, '%') OR d.name LIKE CONCAT('%', #{request.keyword}, '%')) " +
            "</if>" +
            "</script>")
    int countAdminExperiences(@Param("request") ExperienceQueryRequest request);

    @Select("<script>" +
            "SELECT te.id, te.user_id AS userId, te.destination_id AS destinationId, te.title, te.content, te.star, te.status, " +
            "te.reject_reason AS rejectReason, te.create_time AS createTime, te.update_time AS updateTime, " +
            "u.username, d.name AS destinationName, city.name AS cityName, province.name AS provinceName " +
            "FROM travel_experience te " +
            "LEFT JOIN users u ON te.user_id = u.id " +
            "LEFT JOIN destinations d ON te.destination_id = d.id " +
            "LEFT JOIN locations city ON d.location_id = city.id " +
            "LEFT JOIN locations province ON (CASE WHEN city.level = 1 THEN city.id ELSE city.parent_id END) = province.id " +
            "WHERE 1=1 " +
            "<if test='request.status != null'> AND te.status = #{request.status} </if>" +
            "<if test='request.keyword != null and request.keyword != \"\"'> " +
            "AND (te.title LIKE CONCAT('%', #{request.keyword}, '%') OR te.content LIKE CONCAT('%', #{request.keyword}, '%') " +
            "OR u.username LIKE CONCAT('%', #{request.keyword}, '%') OR d.name LIKE CONCAT('%', #{request.keyword}, '%')) " +
            "</if>" +
            "ORDER BY (CASE WHEN te.status = 0 THEN 0 ELSE 1 END), te.id DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<TravelExperience> selectAdminExperiences(@Param("request") ExperienceQueryRequest request,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    @Update("UPDATE travel_experience SET status = #{status}, reject_reason = #{rejectReason}, update_time = NOW() WHERE id = #{id}")
    int updateAuditStatus(@Param("id") Long id, @Param("status") Integer status, @Param("rejectReason") String rejectReason);
}
