package com.example.demo.mapper;

import com.example.demo.dto.DestinationQueryRequest;
import com.example.demo.entity.Category;
import com.example.demo.entity.Destination;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DestinationMapper {

    @Select("<script>" +
            "SELECT DISTINCT d.* FROM destinations d " +
            "<if test='request.tagId != null'>" +
            "INNER JOIN destination_tags dt ON d.id = dt.destination_id AND dt.tag_id = #{request.tagId} " +
            "</if>" +
            "<if test='request.categoryId != null'>" +
            "INNER JOIN destination_category_ref dcr ON d.id = dcr.destination_id AND (dcr.category_id = #{request.categoryId} OR EXISTS (SELECT 1 FROM destination_categories c WHERE c.id = dcr.category_id AND c.parent_id = #{request.categoryId})) " +
            "</if>" +
            "WHERE d.status = 1 " +
            "<if test='request.keyword != null and request.keyword != \"\"'>" +
            "AND (d.name LIKE CONCAT('%', #{request.keyword}, '%') OR d.description LIKE CONCAT('%', #{request.keyword}, '%')) " +
            "</if>" +
            "<if test='request.locationId != null'>" +
            "AND (d.location_id = #{request.locationId} OR EXISTS (SELECT 1 FROM locations l WHERE l.id = d.location_id AND l.parent_id = #{request.locationId})) " +
            "</if>" +
            "<if test='request.minPrice != null'>" +
            "AND d.ticket_price &gt;= #{request.minPrice} " +
            "</if>" +
            "<if test='request.maxPrice != null'>" +
            "AND d.ticket_price &lt;= #{request.maxPrice} " +
            "</if>" +
            "<if test='request.minRating != null'>" +
            "AND d.average_rating &gt;= #{request.minRating} " +
            "</if>" +
            "<if test='request.maxRating != null'>" +
            "AND d.average_rating &lt;= #{request.maxRating} " +
            "</if>" +
            "ORDER BY " +
            "<choose>" +
            "<when test=\"request.sortBy == 'view_count'\">d.view_count</when>" +
            "<when test=\"request.sortBy == 'average_rating'\">d.average_rating</when>" +
            "<when test=\"request.sortBy == 'collect_count'\">d.collect_count</when>" +
            "<when test=\"request.sortBy == 'ticket_price'\">d.ticket_price</when>" +
            "<otherwise>d.view_count</otherwise>" +
            "</choose> " +
            "<if test=\"request.sortOrder == 'asc'\">ASC</if>" +
            "<if test=\"request.sortOrder == 'desc'\">DESC</if>" +
            " LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Destination> selectList(@Param("request") DestinationQueryRequest request, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(DISTINCT d.id) FROM destinations d " +
            "<if test='request.tagId != null'>" +
            "INNER JOIN destination_tags dt ON d.id = dt.destination_id AND dt.tag_id = #{request.tagId} " +
            "</if>" +
            "<if test='request.categoryId != null'>" +
            "INNER JOIN destination_category_ref dcr ON d.id = dcr.destination_id AND (dcr.category_id = #{request.categoryId} OR EXISTS (SELECT 1 FROM destination_categories c WHERE c.id = dcr.category_id AND c.parent_id = #{request.categoryId})) " +
            "</if>" +
            "WHERE d.status = 1 " +
            "<if test='request.keyword != null and request.keyword != \"\"'>" +
            "AND (d.name LIKE CONCAT('%', #{request.keyword}, '%') OR d.description LIKE CONCAT('%', #{request.keyword}, '%')) " +
            "</if>" +
            "<if test='request.locationId != null'>" +
            "AND (d.location_id = #{request.locationId} OR EXISTS (SELECT 1 FROM locations l WHERE l.id = d.location_id AND l.parent_id = #{request.locationId})) " +
            "</if>" +
            "<if test='request.minPrice != null'>" +
            "AND d.ticket_price &gt;= #{request.minPrice} " +
            "</if>" +
            "<if test='request.maxPrice != null'>" +
            "AND d.ticket_price &lt;= #{request.maxPrice} " +
            "</if>" +
            "<if test='request.minRating != null'>" +
            "AND d.average_rating &gt;= #{request.minRating} " +
            "</if>" +
            "<if test='request.maxRating != null'>" +
            "AND d.average_rating &lt;= #{request.maxRating} " +
            "</if>" +
            "</script>")
    int count(@Param("request") DestinationQueryRequest request);

    @Select("SELECT * FROM destinations WHERE id = #{id} AND status = 1")
    Destination findById(@Param("id") Long id);

    @Update("UPDATE destinations SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    @Update("UPDATE destinations SET collect_count = collect_count + 1 WHERE id = #{id}")
    int incrementCollectCount(@Param("id") Long id);

    @Update("UPDATE destinations SET collect_count = collect_count - 1 WHERE id = #{id} AND collect_count > 0")
    int decrementCollectCount(@Param("id") Long id);

    @Select("SELECT * FROM destinations WHERE status = 1 ORDER BY view_count DESC LIMIT #{limit}")
    List<Destination> selectHotDestinations(@Param("limit") int limit);

    @Select("SELECT * FROM destinations WHERE status = 1 ORDER BY average_rating DESC, rating_count DESC LIMIT #{limit}")
    List<Destination> selectTopRatedDestinations(@Param("limit") int limit);

    @Select("<script>" +
            "SELECT d.* FROM destinations d " +
            "INNER JOIN destination_tags dt ON d.id = dt.destination_id " +
            "WHERE d.status = 1 AND dt.tag_id IN " +
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            "GROUP BY d.id " +
            "ORDER BY d.view_count DESC, d.average_rating DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Destination> selectByTagIds(@Param("tagIds") List<Long> tagIds, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT d.* FROM destinations d " +
            "INNER JOIN user_behaviors ub ON d.id = ub.destination_id " +
            "WHERE d.status = 1 AND ub.user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "AND ub.behavior_type IN ('collect', 'view', 'share') " +
            "AND d.id NOT IN (" +
            "SELECT destination_id FROM user_behaviors WHERE user_id = #{currentUserId} AND behavior_type = 'collect'" +
            ") " +
            "GROUP BY d.id " +
            "ORDER BY SUM(CASE ub.behavior_type WHEN 'collect' THEN 3 WHEN 'share' THEN 2 ELSE 1 END) DESC, d.average_rating DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Destination> selectBySimilarUsers(@Param("userIds") List<Long> userIds, @Param("currentUserId") Long currentUserId, @Param("limit") int limit);
    
    @Select("SELECT category_id FROM destination_category_ref WHERE destination_id = #{destinationId}")
    List<Long> selectCategoryIdsByDestinationId(@Param("destinationId") Long destinationId);
    
    @Select("<script>" +
            "SELECT dt.destination_id " +
            "FROM destination_tags dt " +
            "INNER JOIN destinations d ON d.id = dt.destination_id " +
            "WHERE d.status = 1 " +
            "AND dt.destination_id != #{excludeDestinationId} " +
            "<if test='tagIds != null and tagIds.size() > 0'>" +
            "AND dt.tag_id IN " +
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>" +
            "#{tagId}" +
            "</foreach>" +
            "</if>" +
            "<if test='tagIds == null or tagIds.size() == 0'>" +
            "AND 1 = 0 " +
            "</if>" +
            "GROUP BY dt.destination_id " +
            "ORDER BY MAX(d.view_count) DESC, MAX(d.average_rating) DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Long> selectDestinationIdsByTagIds(@Param("tagIds") List<Long> tagIds,
                                            @Param("excludeDestinationId") Long excludeDestinationId,
                                            @Param("limit") Integer limit);
    
    @Select("<script>" +
            "SELECT dcr.destination_id " +
            "FROM destination_category_ref dcr " +
            "INNER JOIN destinations d ON d.id = dcr.destination_id " +
            "INNER JOIN destination_categories dc ON dc.id = dcr.category_id " +
            "WHERE d.status = 1 AND dc.status = 1 " +
            "AND dcr.destination_id != #{excludeDestinationId} " +
            "<if test='categoryIds != null and categoryIds.size() > 0'>" +
            "AND dcr.category_id IN " +
            "<foreach collection='categoryIds' item='categoryId' open='(' separator=',' close=')'>" +
            "#{categoryId}" +
            "</foreach>" +
            "</if>" +
            "<if test='categoryIds == null or categoryIds.size() == 0'>" +
            "AND 1 = 0 " +
            "</if>" +
            "GROUP BY dcr.destination_id " +
            "ORDER BY MAX(d.view_count) DESC, MAX(d.average_rating) DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Long> selectDestinationIdsByCategoryIds(@Param("categoryIds") List<Long> categoryIds,
                                                 @Param("excludeDestinationId") Long excludeDestinationId,
                                                 @Param("limit") Integer limit);
    
    @Select("<script>" +
            "SELECT d.id FROM destinations d " +
            "WHERE d.status = 1 " +
            "AND d.id != #{excludeDestinationId} " +
            "<if test='locationIds != null and locationIds.size() > 0'>" +
            "AND d.location_id IN " +
            "<foreach collection='locationIds' item='locationId' open='(' separator=',' close=')'>" +
            "#{locationId}" +
            "</foreach>" +
            "</if>" +
            "<if test='locationIds == null or locationIds.size() == 0'>" +
            "AND 1 = 0 " +
            "</if>" +
            "ORDER BY d.view_count DESC, d.average_rating DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Long> selectDestinationIdsByLocationIds(@Param("locationIds") List<Long> locationIds,
                                                 @Param("excludeDestinationId") Long excludeDestinationId,
                                                 @Param("limit") Integer limit);
    
    @Select("<script>" +
            "SELECT * FROM destinations WHERE status = 1 " +
            "<if test='ids != null and ids.size() > 0'>" +
            "AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "</if>" +
            "<if test='ids == null or ids.size() == 0'>" +
            "AND 1 = 0 " +
            "</if>" +
            "</script>")
    List<Destination> selectByIds(@Param("ids") List<Long> ids);
    
    @Select("SELECT * FROM destination_categories WHERE id = #{id} AND status = 1")
    Category selectCategoryById(@Param("id") Long id);
}
