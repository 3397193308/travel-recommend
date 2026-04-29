package com.example.demo.mapper;

import com.example.demo.entity.Location;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LocationMapper {

    @Select("SELECT * FROM locations ORDER BY level, code")
    List<Location> selectAll();

    @Select("SELECT * FROM locations WHERE id = #{id}")
    Location findById(Long id);

    @Select("SELECT * FROM locations WHERE parent_id = #{parentId} ORDER BY code")
    List<Location> selectByParentId(Long parentId);

    @Select("SELECT * FROM locations WHERE level = #{level} ORDER BY code")
    List<Location> selectByLevel(Integer level);
}
