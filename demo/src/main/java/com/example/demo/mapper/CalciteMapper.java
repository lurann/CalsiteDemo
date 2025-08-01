package com.example.demo.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CalciteMapper {

    @Select("SELECT  * FROM mysql.t_user as a join pg.t_level as b on a.user_name = b.user_name where a.user_name = #{usename}")
    List<User> selectUsersByUsername(String username);

    @Select("SELECT  * FROM calcite_views.allUsers where username = #{username}")
    List<User> selectUsersByUsername2(@Param("username") String username);
}
