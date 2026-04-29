package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User findByPhone(String phone);

    @Insert("INSERT INTO users (username, password, email, phone, avatar, status, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{avatar}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET email = #{email}, phone = #{phone}, avatar = #{avatar}, age = #{age}, gender = #{gender}, location_id = #{locationId}, update_time = NOW() WHERE id = #{id}")
    int update(User user);

    @Update("UPDATE users SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
