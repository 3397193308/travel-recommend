package com.example.demo.mapper;

import com.example.demo.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM destination_categories WHERE status = 1 ORDER BY sort ASC")
    List<Category> selectAll();

    @Select("SELECT * FROM destination_categories WHERE id = #{id} AND status = 1")
    Category findById(Long id);

    @Select("SELECT * FROM destination_categories WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort ASC")
    List<Category> selectByParentId(Long parentId);

    @Select("SELECT * FROM destination_categories WHERE parent_id = 0 AND status = 1 ORDER BY sort ASC")
    List<Category> selectRootCategories();
}