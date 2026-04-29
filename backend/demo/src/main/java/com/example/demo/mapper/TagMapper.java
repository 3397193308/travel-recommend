package com.example.demo.mapper;

import com.example.demo.entity.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper// 标签映射器
public interface TagMapper {

    @Select("SELECT * FROM tags WHERE status = 1 ORDER BY sort_order, id")
    List<Tag> selectAll();

    @Select("SELECT id, name, #{type} AS type, description, sort_order, status, create_time, update_time " +
            "FROM tags WHERE status = 1 ORDER BY sort_order, id")
    List<Tag> selectByType(@Param("type") String type);

    @Select("SELECT t.* FROM tags t " +
            "INNER JOIN destination_tags dt ON t.id = dt.tag_id " +
            "WHERE dt.destination_id = #{destinationId} AND t.status = 1")
    List<Tag> selectByDestinationId(@Param("destinationId") Long destinationId);
    
    @Select("SELECT t.id FROM tags t " +
            "INNER JOIN destination_tags dt ON t.id = dt.tag_id " +
            "WHERE dt.destination_id = #{destinationId} AND t.status = 1")
    List<Long> selectTagIdsByDestinationId(@Param("destinationId") Long destinationId);

    @Select("SELECT dcr.category_id " +
            "FROM destination_category_ref dcr " +
            "INNER JOIN destination_categories dc ON dc.id = dcr.category_id " +
            "WHERE dcr.destination_id = #{destinationId} AND dc.status = 1")
    List<Long> selectCategoryIdsByDestinationId(@Param("destinationId") Long destinationId);

    @Select("SELECT * FROM tags WHERE id = #{id}")
    Tag findById(@Param("id") Long id);

    @Select("SELECT up.tag_id FROM user_preferences up WHERE up.user_id = #{userId}")
    List<Long> selectPreferenceTagIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT t.id, t.name, up.weight, up.budget_min, up.budget_max FROM tags t " +
            "INNER JOIN user_preferences up ON t.id = up.tag_id " +
            "WHERE up.user_id = #{userId} AND t.status = 1")
    List<java.util.Map<String, Object>> selectUserPreferences(@Param("userId") Long userId);

    @Delete("DELETE FROM user_preferences WHERE user_id = #{userId}")
    int deleteUserPreferences(@Param("userId") Long userId);

    @Insert("INSERT INTO user_preferences (user_id, tag_id, weight, budget_min, budget_max) VALUES (#{userId}, #{tagId}, #{weight}, #{budgetMin}, #{budgetMax})")
    int insertUserPreference(@Param("userId") Long userId, @Param("tagId") Long tagId, @Param("weight") Integer weight, @Param("budgetMin") Integer budgetMin, @Param("budgetMax") Integer budgetMax);
}
