package com.example.client.mapper;

import com.example.client.entity.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {


    @Select("SELECT * FROM ${type}.t_user WHERE user_name = #{userName}")
    List<User> getUserByName(@Param("userName") String userName, @Param("type") String type);


    @Select("${sql}")
    Object executeSelectSql(String sql);

    @Insert("${sql}")
    Object executeInsertSql(String sql);

    @Update("${sql}")
    Object executeUpdateSql(String sql);

    @Delete("${sql}")
    Object executeDeleteSql(String sql);
}
