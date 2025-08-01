package com.example.client.mapper;

import com.example.client.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {


    @Select("SELECT * FROM ${type}.t_user WHERE user_name = #{userName}")
    List<User> getUserByName(@Param("userName") String userName, @Param("type") String type);
}
