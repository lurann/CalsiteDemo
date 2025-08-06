package com.example.client.mapper;

import com.example.client.entity.User;
import com.example.client.entity.UserPg;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {


    @Select("SELECT * FROM ${type}.t_user WHERE username = #{userName}")
    List<User> getUserByName(@Param("userName") String userName, @Param("type") String type);


    @Select("${sql}")
    Object executeSelectSql(String sql);

    @Insert("${sql}")
    Integer executeInsertSql(String sql);

    @Update("${sql}")
    Integer executeUpdateSql(String sql);

    @Delete("${sql}")
    Integer executeDeleteSql(String sql);

    @Select("SELECT * FROM ${type}.t_user WHERE username = #{userName}")
    List<UserPg> getUserByNamePg(@Param("userName") String userName, @Param("type") String type);
}
